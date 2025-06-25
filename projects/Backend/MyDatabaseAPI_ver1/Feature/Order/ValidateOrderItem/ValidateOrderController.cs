
// Features/Orders/ValidateOrderItems/ValidateOrderItemsController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Features.Orders.ValidateOrderItems
{
    [ApiController]
    [Route("api/validate-order-items")]
    public class ValidateOrderItemsController : ControllerBase
    {
        private readonly IMediator _mediator;
        private readonly ILogger<ValidateOrderItemsController> _logger;

        public ValidateOrderItemsController(IMediator mediator, ILogger<ValidateOrderItemsController> logger)
        {
            _mediator = mediator;
            _logger = logger;
        }

        /// <summary>
        /// Validate order items before placing order
        /// </summary>
        /// <param name="request">Validation request</param>
        [HttpPost]
        public async Task<IActionResult> ValidateOrderItems([FromBody] ValidateItemsRequest request)
        {
            try
            {
                _logger.LogInformation("ValidateOrderItems endpoint called with {ItemCount} items", request.Items?.Count ?? 0);

                if (request?.Items == null || !request.Items.Any())
                {
                    return BadRequest(new { error = "Items are required for validation" });
                }

                var command = new ValidateOrderItemsCommand
                {
                    Items = request.Items,
                    CheckStock = request.CheckStock,
                    CheckAvailability = request.CheckAvailability
                };

                var response = await _mediator.Send(command);

                if (response.Success)
                {
                    if (response.IsValid)
                    {
                        return Ok(response);
                    }
                    else
                    {
                        return BadRequest(response); // Return validation errors
                    }
                }
                else
                {
                    return StatusCode(500, new { error = response.Message });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in ValidateOrderItems endpoint");
                return StatusCode(500, new { error = "Internal server error", details = ex.Message });
            }
        }

        /// <summary>
        /// Quick validation endpoint (simplified response)
        /// </summary>
        /// <param name="request">Validation request</param>
        [HttpPost("quick")]
        public async Task<IActionResult> QuickValidateOrderItems([FromBody] ValidateItemsRequest request)
        {
            try
            {
                if (request?.Items == null || !request.Items.Any())
                {
                    return BadRequest(new { error = "Items are required for validation" });
                }

                var command = new ValidateOrderItemsCommand
                {
                    Items = request.Items,
                    CheckStock = request.CheckStock,
                    CheckAvailability = request.CheckAvailability
                };

                var response = await _mediator.Send(command);

                // Simplified response
                return Ok(new
                {
                    isValid = response.IsValid,
                    validItemCount = response.ValidItemCount,
                    invalidItemCount = response.InvalidItemCount,
                    errors = response.Errors,
                    warnings = response.Warnings
                });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in QuickValidateOrderItems endpoint");
                return StatusCode(500, new { error = "Internal server error" });
            }
        }

        /// <summary>
        /// Health check
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new { 
                status = "ValidateOrderItems service is working!", 
                timestamp = DateTime.UtcNow 
            });
        }
    }

    public class ValidateItemsRequest
    {
        public List<OrderItemRequest> Items { get; set; } = new();
        public bool CheckStock { get; set; } = true;
        public bool CheckAvailability { get; set; } = true;
    }
}