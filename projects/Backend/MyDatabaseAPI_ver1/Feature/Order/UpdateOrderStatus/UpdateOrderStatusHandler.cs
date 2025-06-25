
// Features/Orders/UpdateOrderStatus/UpdateOrderStatusHandler.cs
using MediatR;
using MyFoodOrderingAPI.Core.Services;

namespace MyFoodOrderingAPI.Features.Orders.UpdateOrderStatus
{
    public class UpdateOrderStatusHandler : IRequestHandler<UpdateOrderStatusCommand, UpdateOrderStatusResponse>
    {
        private readonly IOrderService _orderService;
        private readonly ILogger<UpdateOrderStatusHandler> _logger;

        public UpdateOrderStatusHandler(IOrderService orderService, ILogger<UpdateOrderStatusHandler> logger)
        {
            _orderService = orderService;
            _logger = logger;
        }

        public async Task<UpdateOrderStatusResponse> Handle(UpdateOrderStatusCommand request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("=== UpdateOrderStatusHandler Started ===");
                _logger.LogInformation("Updating order {OrderId} status to {NewStatus}", request.OrderId, request.NewStatus);

                // Validate status
                var validStatuses = new[] { "Pending", "Confirmed", "Preparing", "Ready", "Delivered", "Cancelled" };
                if (!validStatuses.Contains(request.NewStatus))
                {
                    return new UpdateOrderStatusResponse
                    {
                        OrderId = request.OrderId,
                        Success = false,
                        Message = $"Invalid status '{request.NewStatus}'. Valid statuses are: {string.Join(", ", validStatuses)}"
                    };
                }

                // Get current order to capture old status
                var currentOrder = await _orderService.GetOrderWithDetailsAsync(request.OrderId, cancellationToken);
                if (currentOrder == null)
                {
                    return new UpdateOrderStatusResponse
                    {
                        OrderId = request.OrderId,
                        Success = false,
                        Message = $"Order {request.OrderId} not found"
                    };
                }

                var oldStatus = currentOrder.OrderStatus;

                // Check if status is actually changing
                if (oldStatus == request.NewStatus)
                {
                    return new UpdateOrderStatusResponse
                    {
                        OrderId = request.OrderId,
                        OldStatus = oldStatus,
                        NewStatus = request.NewStatus,
                        UpdatedAt = DateTime.UtcNow,
                        Success = true,
                        Message = $"Order status is already {request.NewStatus}"
                    };
                }

                // Update status using service
                var updatedOrder = await _orderService.UpdateOrderStatusAsync(request.OrderId, request.NewStatus, cancellationToken);

                _logger.LogInformation("✅ Successfully updated order {OrderId} from {OldStatus} to {NewStatus}", 
                    request.OrderId, oldStatus, updatedOrder.OrderStatus);

                return new UpdateOrderStatusResponse
                {
                    OrderId = updatedOrder.Id,
                    OldStatus = oldStatus,
                    NewStatus = updatedOrder.OrderStatus,
                    UpdatedAt = DateTime.UtcNow,
                    Success = true,
                    Message = $"Order status updated from {oldStatus} to {updatedOrder.OrderStatus}"
                };
            }
            catch (ArgumentException ex)
            {
                _logger.LogWarning(ex, "❌ Invalid request for order {OrderId}: {Message}", request.OrderId, ex.Message);
                return new UpdateOrderStatusResponse
                {
                    OrderId = request.OrderId,
                    Success = false,
                    Message = ex.Message
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "❌ Critical error updating order {OrderId} status", request.OrderId);
                return new UpdateOrderStatusResponse
                {
                    OrderId = request.OrderId,
                    Success = false,
                    Message = $"Critical error: {ex.Message}"
                };
            }
        }
    }
}
