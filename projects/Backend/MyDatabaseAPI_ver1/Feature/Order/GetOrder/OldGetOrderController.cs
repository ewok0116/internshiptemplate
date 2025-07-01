
/*

// Features/Orders/GetOrders/GetOrdersController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace MyFoodOrderingAPI.Features.Orders.GetOrders
{
    [ApiController]
    [Route("api/orders")]
    public class GetOrdersController : ControllerBase
    {
        private readonly IMediator _mediator;
        private readonly ILogger<GetOrdersController> _logger;

        public GetOrdersController(IMediator mediator, ILogger<GetOrdersController> logger)
        {
            _mediator = mediator;
            _logger = logger;
        }

        /// <summary>
        /// Get all orders with optional filtering and pagination
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetOrders([FromQuery] GetOrdersQuery query)
        {
            try
            {
                _logger.LogInformation("GetOrders endpoint called");
                
                if (query == null)
                {
                    query = new GetOrdersQuery();
                }

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
                _logger.LogError(ex, "Exception in GetOrders endpoint");
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message
                });
            }
        }

        /// <summary>
        /// Get order by ID with full details
        /// </summary>
        [HttpGet("{id:int}", Name = "GetOrderById")]
        public async Task<IActionResult> GetOrderById(int id)
        {
            try
            {
                _logger.LogInformation("GetOrderById endpoint called with ID: {OrderId}", id);

                if (id <= 0)
                {
                    return BadRequest(new { error = "Invalid order ID" });
                }

                var query = new GetOrderByIdQuery { OrderId = id };
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
                _logger.LogError(ex, "Exception in GetOrderById endpoint with ID: {OrderId}", id);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Get orders by user ID
        /// </summary>
        [HttpGet("user/{userId:int}")]
        public async Task<IActionResult> GetOrdersByUserId(int userId, [FromQuery] int pageNumber = 1, [FromQuery] int pageSize = 10)
        {
            try
            {
                if (userId <= 0)
                {
                    return BadRequest(new { error = "Invalid user ID" });
                }

                var query = new GetOrdersQuery 
                { 
                    UserId = userId,
                    PageNumber = pageNumber,
                    PageSize = pageSize
                };
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
                _logger.LogError(ex, "Exception in GetOrdersByUserId for user {UserId}", userId);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Get orders by status
        /// </summary>
        [HttpGet("status/{status}")]
        public async Task<IActionResult> GetOrdersByStatus(string status, [FromQuery] int pageNumber = 1, [FromQuery] int pageSize = 10)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(status))
                {
                    return BadRequest(new { error = "Status is required" });
                }

                var query = new GetOrdersQuery 
                { 
                    Status = status,
                    PageNumber = pageNumber,
                    PageSize = pageSize
                };
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
                _logger.LogError(ex, "Exception in GetOrdersByStatus for status {Status}", status);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Health check
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new { 
                status = "GetOrders service is working!", 
                timestamp = DateTime.UtcNow 
            });
        }
    }
}

*/