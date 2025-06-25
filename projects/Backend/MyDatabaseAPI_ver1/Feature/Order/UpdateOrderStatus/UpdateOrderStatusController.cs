
// Features/Orders/UpdateOrderStatus/UpdateOrderStatusController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace MyFoodOrderingAPI.Features.Orders.UpdateOrderStatus
{
    [ApiController]
    [Route("api/orders/{orderId}/status")]
    public class UpdateOrderStatusController : ControllerBase
    {
        private readonly IMediator _mediator;
        private readonly ILogger<UpdateOrderStatusController> _logger;

        public UpdateOrderStatusController(IMediator mediator, ILogger<UpdateOrderStatusController> logger)
        {
            _mediator = mediator;
            _logger = logger;
        }

        /// <summary>
        /// Update order status
        /// </summary>
        /// <param name="orderId">Order ID</param>
        /// <param name="request">Status update request</param>
        [HttpPut]
        public async Task<IActionResult> UpdateOrderStatus(int orderId, [FromBody] StatusUpdateRequest request)
        {
            try
            {
                _logger.LogInformation("UpdateOrderStatus endpoint called for order {OrderId}", orderId);

                if (orderId <= 0)
                {
                    return BadRequest(new { error = "Invalid order ID" });
                }

                if (request == null || string.IsNullOrWhiteSpace(request.NewStatus))
                {
                    return BadRequest(new { error = "New status is required" });
                }

                var command = new UpdateOrderStatusCommand
                {
                    OrderId = orderId,
                    NewStatus = request.NewStatus
                };

                var response = await _mediator.Send(command);

                if (response.Success)
                {
                    return Ok(response);
                }
                else
                {
                    return BadRequest(new { error = response.Message });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in UpdateOrderStatus endpoint for order {OrderId}", orderId);
                return StatusCode(500, new { error = "Internal server error", details = ex.Message });
            }
        }

        /// <summary>
        /// Health check for update order status service
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new { 
                status = "UpdateOrderStatus service is working!", 
                timestamp = DateTime.UtcNow 
            });
        }
    }

    // ✅ StatusUpdateRequest is ONLY defined here in the controller file
    public class StatusUpdateRequest
    {
        public string NewStatus { get; set; } = "";
    }
}