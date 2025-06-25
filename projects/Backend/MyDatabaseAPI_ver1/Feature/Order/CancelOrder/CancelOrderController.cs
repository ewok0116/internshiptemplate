
// Features/Orders/CancelOrder/CancelOrderController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace MyFoodOrderingAPI.Features.Orders.CancelOrder
{
    [ApiController]
    [Route("api/cancel-order")] // ✅ CHANGED: Simplified route to avoid conflicts
    public class CancelOrderController : ControllerBase
    {
        private readonly IMediator _mediator;
        private readonly ILogger<CancelOrderController> _logger;

        public CancelOrderController(IMediator mediator, ILogger<CancelOrderController> logger)
        {
            _mediator = mediator;
            _logger = logger;
        }

        /// <summary>
        /// Cancel an order
        /// </summary>
        /// <param name="request">Cancellation request with order ID, user ID, and reason</param>
        [HttpPost]
        public async Task<IActionResult> CancelOrder([FromBody] CancelOrderRequest request)
        {
            try
            {
                _logger.LogInformation("CancelOrder endpoint called for order {OrderId} by user {UserId}", 
                    request?.OrderId, request?.UserId);

                if (request == null)
                {
                    return BadRequest(new { error = "Invalid request body" });
                }

                if (request.OrderId <= 0)
                {
                    return BadRequest(new { error = "Invalid order ID" });
                }

                if (request.UserId <= 0)
                {
                    return BadRequest(new { error = "Valid user ID is required" });
                }

                var command = new CancelOrderCommand
                {
                    OrderId = request.OrderId,
                    UserId = request.UserId,
                    Reason = request.Reason
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
                _logger.LogError(ex, "Exception in CancelOrder endpoint");
                return StatusCode(500, new { error = "Internal server error", details = ex.Message });
            }
        }

        /// <summary>
        /// Cancel order by order ID and user ID (alternative route)
        /// </summary>
        /// <param name="orderId">Order ID</param>
        /// <param name="userId">User ID</param>
        /// <param name="reason">Cancellation reason (optional)</param>
        [HttpPost("{orderId}/user/{userId}")]
        public async Task<IActionResult> CancelOrderByIds(int orderId, int userId, [FromBody] CancelReasonRequest? request = null)
        {
            try
            {
                _logger.LogInformation("CancelOrderByIds endpoint called for order {OrderId} by user {UserId}", orderId, userId);

                if (orderId <= 0)
                {
                    return BadRequest(new { error = "Invalid order ID" });
                }

                if (userId <= 0)
                {
                    return BadRequest(new { error = "Invalid user ID" });
                }

                var command = new CancelOrderCommand
                {
                    OrderId = orderId,
                    UserId = userId,
                    Reason = request?.Reason
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
                _logger.LogError(ex, "Exception in CancelOrderByIds endpoint for order {OrderId}", orderId);
                return StatusCode(500, new { error = "Internal server error", details = ex.Message });
            }
        }

        /// <summary>
        /// Health check for cancel order service
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new { 
                status = "CancelOrder service is working!", 
                timestamp = DateTime.UtcNow 
            });
        }
    }

    // Request DTOs
    public class CancelOrderRequest
    {
        public int OrderId { get; set; }
        public int UserId { get; set; }
        public string? Reason { get; set; }
    }

    public class CancelReasonRequest
    {
        public string? Reason { get; set; }
    }
}