
// WebAPI/Controllers/Categories/GetCategoriesController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;
using MyFoodOrderingAPI.Features.Categories.GetCategories;

namespace MyFoodOrderingAPI.WebAPI.Controllers.Categories
{
    [ApiController]
    [Route("api/categories")]
    public class GetCategoriesController : ControllerBase
    {
        private readonly IMediator _mediator;
        private readonly ILogger<GetCategoriesController> _logger;

        public GetCategoriesController(IMediator mediator, ILogger<GetCategoriesController> logger)
        {
            _mediator = mediator;
            _logger = logger;
        }

        /// <summary>
        /// Get all categories
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetCategories([FromQuery] GetCategoriesQuery query)
        {
            try
            {
                _logger.LogInformation("GetCategories endpoint called");
                
                if (query == null)
                {
                    query = new GetCategoriesQuery();
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
                _logger.LogError(ex, "Exception in GetCategories endpoint");
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Health check for categories service
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new { 
                status = "GetCategories service is working!", 
                timestamp = DateTime.UtcNow 
            });
        }
    }
}