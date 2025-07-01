
// WebAPI/Controllers/OrderItems/GetOrderItemsController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;
using MyFoodOrderingAPI.Features.OrderItems.GetOrderItems;

namespace MyFoodOrderingAPI.WebAPI.Controllers.OrderItems
{
    [ApiController]
    [Route("api/orders/{orderId}/items")]
    public class GetOrderItemsController : ControllerBase
    {
        private readonly IMediator _mediator;
        private readonly ILogger<GetOrderItemsController> _logger;

        public GetOrderItemsController(IMediator mediator, ILogger<GetOrderItemsController> logger)
        {
            _mediator = mediator;
            _logger = logger;
        }

        /// <summary>
        /// Get all items for a specific order
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetOrderItems(int orderId)
        {
            try
            {
                _logger.LogInformation("GetOrderItems endpoint called for order {OrderId}", orderId);

                if (orderId <= 0)
                {
                    return BadRequest(new { error = "Invalid order ID" });
                }

                var query = new GetOrderItemsQuery { OrderId = orderId };
                var response = await _mediator.Send(query);
                
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
                _logger.LogError(ex, "Exception in GetOrderItems endpoint for order {OrderId}", orderId);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Get a specific item from an order
        /// </summary>
        [HttpGet("{itemId}")]
        public async Task<IActionResult> GetOrderItem(int orderId, int itemId)
        {
            try
            {
                _logger.LogInformation("GetOrderItem endpoint called for order {OrderId}, item {ItemId}", orderId, itemId);

                if (orderId <= 0 || itemId <= 0)
                {
                    return BadRequest(new { error = "Invalid order ID or item ID" });
                }

                var query = new GetOrderItemQuery { OrderId = orderId, ItemId = itemId };
                var response = await _mediator.Send(query);
                
                if (response.Success)
                {
                    return Ok(response);
                }
                else
                {
                    return NotFound(new { error = response.Message });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in GetOrderItem endpoint for order {OrderId}, item {ItemId}", orderId, itemId);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Health check for order items service
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new { 
                status = "GetOrderItems service is working!", 
                timestamp = DateTime.UtcNow 
            });
        }
    }
}
