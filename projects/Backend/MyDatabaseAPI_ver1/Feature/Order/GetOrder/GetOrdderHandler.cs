
// Features/Orders/GetOrders/GetOrdersHandler.cs
using MediatR;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Features.Orders.GetOrders
{
    public class GetOrdersHandler : IRequestHandler<GetOrdersQuery, GetOrdersResponse>
    {
        private readonly IOrderRepository _orderRepository;
        private readonly IOrderItemRepository _orderItemRepository;
        private readonly IUserRepository _userRepository;
        private readonly ILogger<GetOrdersHandler> _logger;

        public GetOrdersHandler(
            IOrderRepository orderRepository,
            IOrderItemRepository orderItemRepository,
            IUserRepository userRepository,
            ILogger<GetOrdersHandler> logger)
        {
            _orderRepository = orderRepository;
            _orderItemRepository = orderItemRepository;
            _userRepository = userRepository;
            _logger = logger;
        }

        public async Task<GetOrdersResponse> Handle(GetOrdersQuery request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("=== GetOrdersHandler Started ===");
                _logger.LogInformation("Request - UserId: {UserId}, Status: {Status}, Page: {PageNumber}", 
                    request.UserId, request.Status, request.PageNumber);

                // Create filter from request
                var filter = new OrderFilter
                {
                    UserId = request.UserId,
                    OrderStatus = request.Status,
                    PageNumber = request.PageNumber,
                    PageSize = request.PageSize,
                    OrderDateFrom = request.OrderDateFrom,
                    OrderDateTo = request.OrderDateTo,
                    MinAmount = request.MinAmount,
                    MaxAmount = request.MaxAmount,
                    PaymentMethod = request.PaymentMethod
                };

                _logger.LogInformation("Getting orders from repository...");
                var orderEntities = await _orderRepository.GetOrdersAsync(filter, cancellationToken);
                _logger.LogInformation("✅ Successfully retrieved {Count} orders from database", orderEntities.Count);
                
                var totalCount = await _orderRepository.GetOrdersCountAsync(filter, cancellationToken);
                _logger.LogInformation("✅ Total count: {TotalCount}", totalCount);

                if (orderEntities.Count == 0)
                {
                    _logger.LogInformation("No orders found, returning empty response");
                    return new GetOrdersResponse
                    {
                        Orders = new List<OrderSummaryDto>(),
                        TotalCount = 0,
                        PageNumber = request.PageNumber,
                        PageSize = request.PageSize,
                        TotalPages = 0,
                        Success = true,
                        Message = "No orders found"
                    };
                }

                // Build DTOs
                var orderDtos = new List<OrderSummaryDto>();
                
                for (int i = 0; i < orderEntities.Count; i++)
                {
                    var order = orderEntities[i];
                    _logger.LogDebug("Processing order {Index}/{Total} - OrderId: {OrderId}", 
                        i + 1, orderEntities.Count, order.Id);

                    try
                    {
                        // Get item count
                        int itemCount = 0;
                        try
                        {
                            var orderItems = await _orderItemRepository.GetOrderItemsByOrderIdAsync(order.Id, cancellationToken);
                            itemCount = orderItems.Sum(oi => oi.Quantity);
                        }
                        catch (Exception itemEx)
                        {
                            _logger.LogWarning(itemEx, "Failed to get items for order {OrderId}, using 0", order.Id);
                            itemCount = 0;
                        }

                        // Get user name
                        string userName = "";
                        try
                        {
                            var user = await _userRepository.GetUserByIdAsync(order.UserId, cancellationToken);
                            userName = user?.Name ?? $"User {order.UserId}";
                        }
                        catch (Exception userEx)
                        {
                            _logger.LogWarning(userEx, "Failed to get user {UserId} for order {OrderId}", 
                                order.UserId, order.Id);
                            userName = $"User {order.UserId}";
                        }

                        // Create DTO
                        var dto = new OrderSummaryDto
                        {
                            Id = order.Id,
                            UserId = order.UserId,
                            UserName = userName,
                            OrderStatus = order.OrderStatus,
                            TotalAmount = order.TotalAmount,
                            DeliveryAddress = order.DeliveryAddress,
                            OrderDate = order.OrderDate,
                            PaymentMethod = order.PaymentMethod,
                            ItemCount = itemCount
                        };

                        orderDtos.Add(dto);
                    }
                    catch (Exception orderProcessEx)
                    {
                        _logger.LogError(orderProcessEx, "❌ Failed to process order {OrderId}, skipping", order.Id);
                    }
                }

                var totalPages = (int)Math.Ceiling((double)totalCount / request.PageSize);

                _logger.LogInformation("✅ Successfully processed {ProcessedCount}/{TotalCount} orders", 
                    orderDtos.Count, orderEntities.Count);

                return new GetOrdersResponse
                {
                    Orders = orderDtos,
                    TotalCount = totalCount,
                    PageNumber = request.PageNumber,
                    PageSize = request.PageSize,
                    TotalPages = totalPages,
                    Success = true,
                    Message = $"Successfully retrieved {orderDtos.Count} orders"
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "❌ Critical error in GetOrdersHandler");
                return new GetOrdersResponse
                {
                    Orders = new List<OrderSummaryDto>(),
                    TotalCount = 0,
                    PageNumber = request.PageNumber,
                    PageSize = request.PageSize,
                    TotalPages = 0,
                    Success = false,
                    Message = $"Critical error: {ex.Message}"
                };
            }
        }
    }
}
