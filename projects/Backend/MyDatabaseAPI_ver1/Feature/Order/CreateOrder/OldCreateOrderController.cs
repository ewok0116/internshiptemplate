/*
// Features/Orders/CreateOrder/CreateOrderController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace MyFoodOrderingAPI.Features.Orders.CreateOrder
{
    [ApiController]
    [Route("api/create-order")]
    public class CreateOrderController : ControllerBase
    {
        private readonly IMediator _mediator;
        private readonly ILogger<CreateOrderController> _logger;

        public CreateOrderController(IMediator mediator, ILogger<CreateOrderController> logger)
        {
            _mediator = mediator;
            _logger = logger;
        }

        /// <summary>
        /// Create a new order
        /// </summary>
        /// <param name="command">Order creation command</param>
        /// <returns>Created order details</returns>
        [HttpPost]
        public async Task<IActionResult> CreateOrder([FromBody] CreateOrderCommand command)
        {
            try
            {
                _logger.LogInformation("CreateOrder endpoint called for user {UserId}", command?.UserId);

                if (command == null)
                {
                    return BadRequest(new { error = "Invalid request body" });
                }

                var response = await _mediator.Send(command);
                
                if (response.Success)
                {
                    _logger.LogInformation("Order {OrderId} created successfully", response.OrderId);
                    return CreatedAtRoute(
                        "GetOrderById",
                        new { id = response.OrderId }, 
                        response);
                }
                else
                {
                    _logger.LogWarning("Order creation failed: {Message}", response.Message);
                    return BadRequest(new { error = response.Message });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in CreateOrder endpoint");
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Health check for create order service
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new { 
                status = "CreateOrder service is working!", 
                timestamp = DateTime.UtcNow 
            });
        }
    }
}

*/