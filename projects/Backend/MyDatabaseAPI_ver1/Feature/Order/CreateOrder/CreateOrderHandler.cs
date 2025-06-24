// Features/Orders/CreateOrder/CreateOrderHandler.cs
using MediatR;
using Microsoft.Data.SqlClient;

namespace MyFoodOrderingAPI.Features.Orders.CreateOrder
{
    public class CreateOrderHandler : IRequestHandler<CreateOrderCommand, CreateOrderResponse>
    {
        private readonly string _connectionString;

        public CreateOrderHandler(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection") ??
                throw new InvalidOperationException("Connection string not found.");
        }

        public async Task<CreateOrderResponse> Handle(CreateOrderCommand request, CancellationToken cancellationToken)
        {
            try
            {
                // ✅ VALIDATE INPUT
                var validationResult = ValidateRequest(request);
                if (!validationResult.IsValid)
                {
                    return new CreateOrderResponse
                    {
                        Success = false,
                        Message = validationResult.ErrorMessage
                    };
                }

                // ✅ VALIDATE USER EXISTS
                if (!await UserExists(request.UserId, cancellationToken))
                {
                    return new CreateOrderResponse
                    {
                        Success = false,
                        Message = $"User {request.UserId} not found"
                    };
                }

                // ✅ VALIDATE PRODUCTS AND GET PRICES (FROM YOUR PPrice COLUMN)
                var productInfo = await ValidateAndGetProductInfo(request.OrderItems, cancellationToken);
                
                // ✅ CALCULATE TOTAL AMOUNT
                decimal totalAmount = 0;
                foreach (var item in request.OrderItems)
                {
                    var productPrice = productInfo[item.ProductId].Price;
                    var itemTotal = item.Quantity * productPrice;
                    totalAmount += itemTotal;
                }
                
                // ✅ SAVE ORDER AND ORDER ITEMS WITH TRANSACTION
                var orderId = await SaveOrderWithItems(request, productInfo, totalAmount, cancellationToken);

                // ✅ BUILD RESPONSE WITH ORDER ITEM DETAILS
                var orderItemDtos = new List<CreatedOrderItemDto>();
                for (int i = 0; i < request.OrderItems.Count; i++)
                {
                    var item = request.OrderItems[i];
                    var productPrice = productInfo[item.ProductId].Price;
                    var itemTotal = item.Quantity * productPrice;
                    
                    orderItemDtos.Add(new CreatedOrderItemDto
                    {
                        Id = i + 1,
                        ProductId = item.ProductId,
                        ProductName = productInfo[item.ProductId].Name,
                        Quantity = item.Quantity,
                        UnitPrice = productPrice,
                        TotalPrice = itemTotal
                    });
                }

                return new CreateOrderResponse
                {
                    OrderId = orderId,
                    OrderStatus = "Pending",
                    TotalAmount = totalAmount,
                    OrderDate = DateTime.UtcNow,
                    DeliveryAddress = request.DeliveryAddress,
                    PaymentMethod = request.PaymentMethod,
                    OrderItems = orderItemDtos,
                    Success = true,
                    Message = "Order created successfully"
                };
            }
            catch (Exception ex)
            {
                return new CreateOrderResponse
                {
                    Success = false,
                    Message = $"Failed to create order: {ex.Message}"
                };
            }
        }

        // ✅ VALIDATE REQUEST INPUT
        private (bool IsValid, string ErrorMessage) ValidateRequest(CreateOrderCommand request)
        {
            if (request.UserId <= 0)
                return (false, "Invalid user ID");

            if (string.IsNullOrWhiteSpace(request.DeliveryAddress))
                return (false, "Delivery address is required");

            if (string.IsNullOrWhiteSpace(request.PaymentMethod))
                return (false, "Payment method is required");

            if (request.OrderItems == null || !request.OrderItems.Any())
                return (false, "Order must contain at least one item");

            // Validate each order item
            foreach (var item in request.OrderItems)
            {
                if (item.ProductId <= 0)
                    return (false, $"Invalid product ID: {item.ProductId}");

                if (item.Quantity <= 0)
                    return (false, $"Quantity must be greater than zero for product {item.ProductId}");

                if (item.Quantity > 100)
                    return (false, $"Maximum quantity is 100 per product. Product {item.ProductId} has quantity {item.Quantity}");
            }

            // Check for duplicate products in the same order
            var duplicates = request.OrderItems
                .GroupBy(x => x.ProductId)
                .Where(g => g.Count() > 1)
                .Select(g => g.Key);

            if (duplicates.Any())
            {
                return (false, $"Duplicate products found: {string.Join(", ", duplicates)}. Each product should appear only once per order.");
            }

            return (true, "");
        }

        // ✅ CHECK IF USER EXISTS
        private async Task<bool> UserExists(int userId, CancellationToken cancellationToken)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);

            using var command = new SqlCommand("SELECT COUNT(*) FROM Users WHERE UID = @UserId", connection);
            command.Parameters.AddWithValue("@UserId", userId);

            var count = (int)await command.ExecuteScalarAsync(cancellationToken);
            return count > 0;
        }

        // ✅ VALIDATE PRODUCTS AND GET INFO FROM YOUR DATABASE
        private async Task<Dictionary<int, (string Name, decimal Price)>> ValidateAndGetProductInfo(
            List<CreateOrderItemDto> items, 
            CancellationToken cancellationToken)
        {
            var productInfo = new Dictionary<int, (string Name, decimal Price)>();

            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);

            foreach (var item in items)
            {
                using var command = new SqlCommand(
                    "SELECT PName, Price FROM Products WHERE PID = @ProductId AND IsAvailable = 1", 
                    connection);
                command.Parameters.AddWithValue("@ProductId", item.ProductId);

                using var reader = await command.ExecuteReaderAsync(cancellationToken);
                if (await reader.ReadAsync(cancellationToken))
                {
                    var name = reader["PName"]?.ToString() ?? "";
                    var price = Convert.ToDecimal(reader["Price"]);
                    productInfo[item.ProductId] = (name, price);
                }
                else
                {
                    throw new InvalidOperationException($"Product {item.ProductId} not found or not available");
                }
            }

            return productInfo;
        }

        // ✅ SAVE WITH TRANSACTION - ALL OR NOTHING (WITHOUT CREATING OrderItem OBJECTS)
        private async Task<int> SaveOrderWithItems(
            CreateOrderCommand request,
            Dictionary<int, (string Name, decimal Price)> productInfo,
            decimal totalAmount,
            CancellationToken cancellationToken)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            using var transaction = connection.BeginTransaction();

            try
            {
                // Step 1: Save Order
                var orderCommand = new SqlCommand(@"
                    INSERT INTO Orders (UID, OrderStatus, TotalAmount, DeliveryAddress, OrderDate, PaymentMethod) 
                    VALUES (@UserId, @Status, @TotalAmount, @DeliveryAddress, @OrderDate, @PaymentMethod); 
                    SELECT SCOPE_IDENTITY();", connection, transaction);

                orderCommand.Parameters.AddWithValue("@UserId", request.UserId);
                orderCommand.Parameters.AddWithValue("@Status", "Pending");
                orderCommand.Parameters.AddWithValue("@TotalAmount", totalAmount);
                orderCommand.Parameters.AddWithValue("@DeliveryAddress", request.DeliveryAddress);
                orderCommand.Parameters.AddWithValue("@OrderDate", DateTime.UtcNow);
                orderCommand.Parameters.AddWithValue("@PaymentMethod", request.PaymentMethod);

                var result = await orderCommand.ExecuteScalarAsync(cancellationToken);
                var orderId = Convert.ToInt32(result);

                // Step 2: Save OrderItems
                foreach (var item in request.OrderItems)
                {
                    var productPrice = productInfo[item.ProductId].Price;
                    
                    var itemCommand = new SqlCommand(@"
                        INSERT INTO OrderItems (OID, PID, Quantity, UnitPrice) 
                        VALUES (@OrderId, @ProductId, @Quantity, @UnitPrice)", 
                        connection, transaction);

                    itemCommand.Parameters.AddWithValue("@OrderId", orderId);
                    itemCommand.Parameters.AddWithValue("@ProductId", item.ProductId);
                    itemCommand.Parameters.AddWithValue("@Quantity", item.Quantity);
                    itemCommand.Parameters.AddWithValue("@UnitPrice", productPrice);

                    await itemCommand.ExecuteNonQueryAsync(cancellationToken);
                }

                // Step 3: Commit transaction
                transaction.Commit();
                return orderId;
            }
            catch (Exception ex)
            {
                // Step 4: Rollback if anything fails
                transaction.Rollback();
                throw new Exception($"Database error: {ex.Message}", ex);
            }
        }
    }
}