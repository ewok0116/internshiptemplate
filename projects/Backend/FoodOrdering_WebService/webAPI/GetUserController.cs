
// WebAPI/Controllers/Users/GetUsersController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;
using MyFoodOrderingAPI.Features.Users.GetUsers;

namespace MyFoodOrderingAPI.WebAPI.Controllers.Users
{
    [ApiController]
    [Route("api/users")]
    public class GetUsersController : ControllerBase
    {
        private readonly IMediator _mediator;
        private readonly ILogger<GetUsersController> _logger;

        public GetUsersController(IMediator mediator, ILogger<GetUsersController> logger)
        {
            _mediator = mediator;
            _logger = logger;
        }

        /// <summary>
        /// Get all users with optional filtering and pagination
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetUsers([FromQuery] GetUsersQuery query)
        {
            try
            {
                _logger.LogInformation("GetUsers endpoint called");
                
                if (query == null)
                {
                    query = new GetUsersQuery();
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
                _logger.LogError(ex, "Exception in GetUsers endpoint");
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Get user by ID
        /// </summary>
        [HttpGet("{id:int}", Name = "GetUserById")]
        public async Task<IActionResult> GetUserById(int id)
        {
            try
            {
                _logger.LogInformation("GetUserById endpoint called with ID: {UserId}", id);

                if (id <= 0)
                {
                    return BadRequest(new { error = "Invalid user ID" });
                }

                // Use the existing GetUsersQuery - it will return all users, then filter by ID
                var query = new GetUsersQuery { PageSize = 1000 }; // Get more users to find the specific one
                var response = await _mediator.Send(query);

                if (response.Success && response.Data?.Any() == true)
                {
                    var user = response.Data.FirstOrDefault(u => u.Id == id);
                    if (user != null)
                    {
                        return Ok(new GetUsersResponse
                        {
                            Data = new List<UserDto> { user },
                            TotalCount = 1,
                            Success = true,
                            Message = "User retrieved successfully"
                        });
                    }
                    else
                    {
                        return NotFound(new { error = "User not found" });
                    }
                }
                else
                {
                    return NotFound(new { error = response.Message ?? "User not found" });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in GetUserById endpoint with ID: {UserId}", id);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Get user by email
        /// </summary>
        [HttpGet("email/{email}")]
        public async Task<IActionResult> GetUserByEmail(string email)
        {
            try
            {
                _logger.LogInformation("GetUserByEmail endpoint called with email: {Email}", email);

                if (string.IsNullOrWhiteSpace(email))
                {
                    return BadRequest(new { error = "Email is required" });
                }

                // Use the existing GetUsersQuery - it will return all users, then filter by email
                var query = new GetUsersQuery { PageSize = 1000 }; // Get more users to find the specific one
                var response = await _mediator.Send(query);

                if (response.Success && response.Data?.Any() == true)
                {
                    var user = response.Data.FirstOrDefault(u => u.Email.Equals(email, StringComparison.OrdinalIgnoreCase));
                    if (user != null)
                    {
                        return Ok(new GetUsersResponse
                        {
                            Data = new List<UserDto> { user },
                            TotalCount = 1,
                            Success = true,
                            Message = "User retrieved successfully"
                        });
                    }
                    else
                    {
                        return NotFound(new { error = "User not found" });
                    }
                }
                else
                {
                    return NotFound(new { error = response.Message ?? "User not found" });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in GetUserByEmail endpoint with email: {Email}", email);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Health check for users service
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new { 
                status = "GetUsers service is working!", 
                timestamp = DateTime.UtcNow 
            });
        }
    }
}
