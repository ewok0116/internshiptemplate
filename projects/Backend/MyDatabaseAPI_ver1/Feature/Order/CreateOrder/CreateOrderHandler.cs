
// Features/Orders/CreateOrder/CreateOrderHandler.cs
using MediatR;
using MyFoodOrderingAPI.Core.Services;
using MyFoodOrderingAPI.Core.Models;
using MyFoodOrderingAPI.Core.Interfaces;

namespace MyFoodOrderingAPI.Features.Orders.CreateOrder
{
    public class CreateOrderHandler : IRequestHandler<CreateOrderCommand, CreateOrderResponse>
    {
        private readonly IOrderService _orderService;
        private readonly IProductRepository _productRepository;
        private readonly ILogger<CreateOrderHandler> _logger;

        public CreateOrderHandler(
            IOrderService orderService,
            IProductRepository productRepository,
            ILogger<CreateOrderHandler> logger)
        {
            _orderService = orderService;
            _productRepository = productRepository;
            _logger = logger;
        }

        public async Task<CreateOrderResponse> Handle(CreateOrderCommand request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("=== CreateOrderHandler Started ===");
                _logger.LogInformation("Creating order for user {UserId} with {ItemCount} items", 
                    request.UserId, request.OrderItems?.Count ?? 0);

                // ✅ VALIDATE INPUT
                var validationResult = ValidateRequest(request);
                if (!validationResult.IsValid)
                {
                    _logger.LogWarning("Validation failed: {ErrorMessage}", validationResult.ErrorMessage);
                    return new CreateOrderResponse
                    {
                        Success = false,
                        Message = validationResult.ErrorMessage
                    };
                }

                // 🎯 Create service request model
                var createOrderRequest = new CreateOrderRequest
                {
                    UserId = request.UserId,
                    DeliveryAddress = request.DeliveryAddress,
                    PaymentMethod = request.PaymentMethod,
                    Items = request.OrderItems.Select(item => new OrderItemRequest
                    {
                        ProductId = item.ProductId,
                        Quantity = item.Quantity
                    }).ToList()
                };

                _logger.LogInformation("Calling OrderService.CreateOrderAsync...");

                // 🎯 Use service to create order
                var order = await _orderService.CreateOrderAsync(createOrderRequest, cancellationToken);
                
                _logger.LogInformation("✅ Order created with ID: {OrderId}", order.Id);

                // 🎯 Get order with details for response
                _logger.LogInformation("Getting order details...");
                var orderWithDetails = await _orderService.GetOrderWithDetailsAsync(order.Id, cancellationToken);

                // ✅ BUILD RESPONSE WITH ORDER ITEM DETAILS (with product names)
                var orderItemDtos = new List<CreatedOrderItemDto>();
                if (orderWithDetails?.OrderItems != null)
                {
                    _logger.LogInformation("Processing {Count} order items", orderWithDetails.OrderItems.Count);
                    
                    for (int i = 0; i < orderWithDetails.OrderItems.Count; i++)
                    {
                        var item = orderWithDetails.OrderItems[i];
                        
                        // Get product name
                        string productName = "";
                        try
                        {
                            var product = await _productRepository.GetProductByIdAsync(item.ProductId, cancellationToken);
                            productName = product?.Name ?? $"Product {item.ProductId}";
                        }
                        catch (Exception ex)
                        {
                            _logger.LogWarning(ex, "Failed to get product name for {ProductId}", item.ProductId);
                            productName = $"Product {item.ProductId}";
                        }
                        
                        orderItemDtos.Add(new CreatedOrderItemDto
                        {
                            Id = item.Id,
                            ProductId = item.ProductId,
                            ProductName = productName,
                            Quantity = item.Quantity,
                            UnitPrice = item.UnitPrice,
                            TotalPrice = item.TotalPrice
                        });
                    }
                }

                _logger.LogInformation("✅ Successfully created order {OrderId} for user {UserId} with total {TotalAmount}", 
                    order.Id, request.UserId, order.TotalAmount);

                return new CreateOrderResponse
                {
                    OrderId = order.Id,
                    TotalAmount = order.TotalAmount,
                    OrderDate = order.OrderDate,
                    DeliveryAddress = order.DeliveryAddress,
                    PaymentMethod = order.PaymentMethod,
                    OrderItems = orderItemDtos,
                    Success = true,
                    Message = "Order created successfully"
                };
            }
            catch (ArgumentException ex)
            {
                // Business validation errors
                _logger.LogWarning(ex, "❌ Validation error creating order for user {UserId}: {Message}", 
                    request.UserId, ex.Message);
                return new CreateOrderResponse
                {
                    Success = false,
                    Message = ex.Message
                };
            }
            catch (Exception ex)
            {
                // Unexpected errors
                _logger.LogError(ex, "❌ Critical error creating order for user {UserId}", request.UserId);
                return new CreateOrderResponse
                {
                    Success = false,
                    Message = $"Critical error: {ex.Message}"
                };
            }
        }

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
    }
}
