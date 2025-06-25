
// Features/Orders/CancelOrder/CancelOrderHandler.cs
using MediatR;
using MyFoodOrderingAPI.Core.Services;

namespace MyFoodOrderingAPI.Features.Orders.CancelOrder
{
    public class CancelOrderHandler : IRequestHandler<CancelOrderCommand, CancelOrderResponse>
    {
        private readonly IOrderService _orderService;
        private readonly ILogger<CancelOrderHandler> _logger;

        public CancelOrderHandler(IOrderService orderService, ILogger<CancelOrderHandler> logger)
        {
            _orderService = orderService;
            _logger = logger;
        }

        public async Task<CancelOrderResponse> Handle(CancelOrderCommand request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("=== CancelOrderHandler Started ===");
                _logger.LogInformation("Cancelling order {OrderId} for user {UserId}", request.OrderId, request.UserId);

                // Get current order to capture previous status
                var currentOrder = await _orderService.GetOrderWithDetailsAsync(request.OrderId, cancellationToken);
                var previousStatus = currentOrder?.OrderStatus ?? "Unknown";

                // Cancel order using service
                var result = await _orderService.CancelOrderAsync(request.OrderId, request.UserId, cancellationToken);

                if (!result)
                {
                    return new CancelOrderResponse
                    {
                        OrderId = request.OrderId,
                        UserId = request.UserId,
                        Success = false,
                        Message = "Order not found or already processed"
                    };
                }

                _logger.LogInformation("✅ Successfully cancelled order {OrderId} for user {UserId}", 
                    request.OrderId, request.UserId);

                return new CancelOrderResponse
                {
                    OrderId = request.OrderId,
                    UserId = request.UserId,
                    Reason = request.Reason,
                    CancelledAt = DateTime.UtcNow,
                    PreviousStatus = previousStatus,
                    Success = true,
                    Message = "Order cancelled successfully"
                };
            }
            catch (UnauthorizedAccessException ex)
            {
                _logger.LogWarning(ex, "❌ Unauthorized cancellation attempt for order {OrderId} by user {UserId}", 
                    request.OrderId, request.UserId);
                return new CancelOrderResponse
                {
                    OrderId = request.OrderId,
                    UserId = request.UserId,
                    Success = false,
                    Message = "Order does not belong to user"
                };
            }
            catch (InvalidOperationException ex)
            {
                _logger.LogWarning(ex, "❌ Invalid cancellation attempt for order {OrderId}: {Message}", 
                    request.OrderId, ex.Message);
                return new CancelOrderResponse
                {
                    OrderId = request.OrderId,
                    UserId = request.UserId,
                    Success = false,
                    Message = ex.Message
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "❌ Critical error cancelling order {OrderId}", request.OrderId);
                return new CancelOrderResponse
                {
                    OrderId = request.OrderId,
                    UserId = request.UserId,
                    Success = false,
                    Message = $"Critical error: {ex.Message}"
                };
            }
        }
    }
}
