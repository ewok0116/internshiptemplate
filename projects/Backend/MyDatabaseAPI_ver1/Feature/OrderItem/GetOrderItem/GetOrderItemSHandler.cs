// Features/OrderItems/GetOrderItems/GetOrderItemsHandler.cs
using MediatR;
using Microsoft.Data.SqlClient;

namespace MyFoodOrderingAPI.Features.OrderItems.GetOrderItems
{
    public class GetOrderItemsHandler : IRequestHandler<GetOrderItemsQuery, GetOrderItemsResponse>
    {
        private readonly string _connectionString;

        public GetOrderItemsHandler(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection") ??
                throw new InvalidOperationException("Connection string not found.");
        }

        public async Task<GetOrderItemsResponse> Handle(GetOrderItemsQuery request, CancellationToken cancellationToken)
        {
            try
            {
                // ✅ CHECK IF ORDER EXISTS FIRST
                if (!await OrderExists(request.OrderId, cancellationToken))
                {
                    return new GetOrderItemsResponse
                    {
                        Success = false,
                        Message = $"Order {request.OrderId} not found"
                    };
                }

                var orderItems = new List<OrderItemDetailDto>();

                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync(cancellationToken);

                // ✅ GET ORDER ITEMS WITH COMPLETE PRODUCT DETAILS
                using var command = new SqlCommand(@"
                    SELECT 
                        oi.OIID, oi.OID, oi.PID, oi.Quantity, oi.UnitPrice, oi.TotalPrice,
                        p.PName, p.PDescription, p.ImageUrl
                    FROM OrderItems oi
                    JOIN Products p ON oi.PID = p.PID
                    WHERE oi.OID = @OrderId
                    ORDER BY oi.OIID", connection);

                command.Parameters.AddWithValue("@OrderId", request.OrderId);

                using var reader = await command.ExecuteReaderAsync(cancellationToken);
                while (await reader.ReadAsync(cancellationToken))
                {
                    var orderItem = new OrderItemDetailDto
                    {
                        Id = Convert.ToInt32(reader["OIID"]),
                        OrderId = Convert.ToInt32(reader["OID"]),
                        ProductId = Convert.ToInt32(reader["PID"]),
                        ProductName = reader["PName"]?.ToString() ?? "",
                        ProductDescription = reader["PDescription"]?.ToString() ?? "",
                        ProductImageUrl = reader["ImageUrl"]?.ToString() ?? "",
                        Quantity = Convert.ToInt32(reader["Quantity"]),
                        UnitPrice = Convert.ToDecimal(reader["UnitPrice"]),
                        TotalPrice = Convert.ToDecimal(reader["TotalPrice"])
                    };

                    // ✅ VALIDATE CALCULATION
                    var calculatedTotal = Math.Round(orderItem.Quantity * orderItem.UnitPrice, 2);
                    orderItem.IsValid = Math.Abs(orderItem.TotalPrice - calculatedTotal) < 0.01m;

                    orderItems.Add(orderItem);
                }

                return new GetOrderItemsResponse
                {
                    OrderItems = orderItems,
                    TotalAmount = orderItems.Sum(x => x.TotalPrice),
                    TotalItems = orderItems.Sum(x => x.Quantity),
                    Success = true,
                    Message = $"Found {orderItems.Count} order items for order {request.OrderId}"
                };
            }
            catch (Exception ex)
            {
                return new GetOrderItemsResponse
                {
                    Success = false,
                    Message = ex.Message
                };
            }
        }

        // ✅ HELPER METHOD TO CHECK IF ORDER EXISTS
        private async Task<bool> OrderExists(int orderId, CancellationToken cancellationToken)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);

            using var command = new SqlCommand("SELECT COUNT(*) FROM Orders WHERE OID = @OrderId", connection);
            command.Parameters.AddWithValue("@OrderId", orderId);

            var count = (int)await command.ExecuteScalarAsync(cancellationToken);
            return count > 0;
        }
    }
}