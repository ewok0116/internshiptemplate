
// Features/Orders/GetOrders/GetOrderByIdHandler.cs
using MediatR;
using MyFoodOrderingAPI.Core.Interfaces;

namespace MyFoodOrderingAPI.Features.Orders.GetOrders
{
    public class GetOrderByIdHandler : IRequestHandler<GetOrderByIdQuery, GetOrderByIdResponse>
    {
        private readonly IOrderRepository _orderRepository;
        private readonly IOrderItemRepository _orderItemRepository;
        private readonly IUserRepository _userRepository;
        private readonly IProductRepository _productRepository;
        private readonly ILogger<GetOrderByIdHandler> _logger;

        public GetOrderByIdHandler(
            IOrderRepository orderRepository,
            IOrderItemRepository orderItemRepository,
            IUserRepository userRepository,
            IProductRepository productRepository,
            ILogger<GetOrderByIdHandler> logger)
        {
            _orderRepository = orderRepository;
            _orderItemRepository = orderItemRepository;
            _userRepository = userRepository;
            _productRepository = productRepository;
            _logger = logger;
        }

        public async Task<GetOrderByIdResponse> Handle(GetOrderByIdQuery request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("=== GetOrderByIdHandler Started ===");
                _logger.LogInformation("Retrieving order {OrderId}", request.OrderId);

                if (request.OrderId <= 0)
                {
                    return new GetOrderByIdResponse
                    {
                        Success = false,
                        Message = "Invalid order ID"
                    };
                }

                // Get order
                var order = await _orderRepository.GetOrderByIdAsync(request.OrderId, cancellationToken);
                if (order == null)
                {
                    return new GetOrderByIdResponse
                    {
                        Success = false,
                        Message = "Order not found"
                    };
                }

                _logger.LogInformation("✅ Found order {OrderId}", order.Id);

                // Get user name
                string userName = "";
                try
                {
                    var user = await _userRepository.GetUserByIdAsync(order.UserId, cancellationToken);
                    userName = user?.Name ?? $"User {order.UserId}";
                }
                catch (Exception userEx)
                {
                    _logger.LogWarning(userEx, "Failed to get user {UserId}", order.UserId);
                    userName = $"User {order.UserId}";
                }

                // Build order detail DTO
                var orderDetail = new OrderDetailDto
                {
                    Id = order.Id,
                    UserId = order.UserId,
                    UserName = userName,
                    TotalAmount = order.TotalAmount,
                    DeliveryAddress = order.DeliveryAddress,
                    OrderDate = order.OrderDate,
                    PaymentMethod = order.PaymentMethod,
                    OrderItems = new List<OrderItemDetailDto>()
                };

                // Get order items
                try
                {
                    var orderItems = await _orderItemRepository.GetOrderItemsByOrderIdAsync(order.Id, cancellationToken);
                    _logger.LogInformation("Found {Count} order items", orderItems.Count);

                    foreach (var orderItem in orderItems)
                    {
                        try
                        {
                            string productName = "";
                            try
                            {
                                var product = await _productRepository.GetProductByIdAsync(orderItem.ProductId, cancellationToken);
                                productName = product?.Name ?? $"Product {orderItem.ProductId}";
                            }
                            catch (Exception productEx)
                            {
                                _logger.LogWarning(productEx, "Failed to get product {ProductId}", orderItem.ProductId);
                                productName = $"Product {orderItem.ProductId}";
                            }
                            
                            orderDetail.OrderItems.Add(new OrderItemDetailDto
                            {
                                Id = orderItem.Id,
                                ProductId = orderItem.ProductId,
                                ProductName = productName,
                                Quantity = orderItem.Quantity,
                                UnitPrice = orderItem.UnitPrice,
                                TotalPrice = orderItem.TotalPrice
                            });
                        }
                        catch (Exception itemEx)
                        {
                            _logger.LogError(itemEx, "Failed to process order item {ItemId}", orderItem.Id);
                        }
                    }
                }
                catch (Exception itemsEx)
                {
                    _logger.LogError(itemsEx, "Failed to get order items for order {OrderId}", order.Id);
                }

                _logger.LogInformation("✅ Successfully retrieved order {OrderId} with {ItemCount} items", 
                    request.OrderId, orderDetail.OrderItems.Count);

                return new GetOrderByIdResponse
                {
                    Order = orderDetail,
                    Success = true,
                    Message = "Order found successfully"
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "❌ Critical error retrieving order {OrderId}", request.OrderId);
                return new GetOrderByIdResponse
                {
                    Success = false,
                    Message = $"Critical error: {ex.Message}"
                };
            }
        }
    }
}
