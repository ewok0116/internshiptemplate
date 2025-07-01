// WebAPI/Controllers/Products/GetProductsController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;
using MyFoodOrderingAPI.Features.Products.GetProducts;

namespace MyFoodOrderingAPI.WebAPI.Controllers.Products
{
    [ApiController]
    [Route("api/products")]
    public class GetProductsController : ControllerBase
    {
        private readonly IMediator _mediator;
        private readonly ILogger<GetProductsController> _logger;

        public GetProductsController(IMediator mediator, ILogger<GetProductsController> logger)
        {
            _mediator = mediator;
            _logger = logger;
        }

        /// <summary>
        /// Get all products with optional filtering and pagination
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetProducts([FromQuery] GetProductsQuery query)
        {
            try
            {
                _logger.LogInformation("GetProducts endpoint called");
                
                if (query == null)
                {
                    query = new GetProductsQuery();
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
                _logger.LogError(ex, "Exception in GetProducts endpoint");
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Get product by ID
        /// </summary>
        [HttpGet("{id:int}", Name = "GetProductById")]
        public async Task<IActionResult> GetProductById(int id)
        {
            try
            {
                _logger.LogInformation("GetProductById endpoint called with ID: {ProductId}", id);

                if (id <= 0)
                {
                    return BadRequest(new { error = "Invalid product ID" });
                }

                // Use the existing GetProductsQuery with a larger page size to get all products, then filter
                var query = new GetProductsQuery 
                { 
                    PageSize = 1000, // Get enough products to find the one we need
                    IsAvailable = null // Don't filter by availability when looking for specific product
                };
                var response = await _mediator.Send(query);

                if (response.Success && response.Data?.Any() == true)
                {
                    var product = response.Data.FirstOrDefault(p => p.Id == id);
                    if (product != null)
                    {
                        return Ok(new GetProductsResponse
                        {
                            Data = new List<ProductDto> { product },
                            TotalCount = 1,
                            Success = true,
                            Message = "Product retrieved successfully"
                        });
                    }
                    else
                    {
                        return NotFound(new { error = "Product not found" });
                    }
                }
                else
                {
                    return NotFound(new { error = response.Message ?? "Product not found" });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in GetProductById endpoint with ID: {ProductId}", id);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Get products by category
        /// </summary>
        [HttpGet("category/{categoryId:int}")]
        public async Task<IActionResult> GetProductsByCategory(int categoryId, [FromQuery] int pageNumber = 1, [FromQuery] int pageSize = 10)
        {
            try
            {
                _logger.LogInformation("GetProductsByCategory endpoint called with category ID: {CategoryId}", categoryId);

                if (categoryId <= 0)
                {
                    return BadRequest(new { error = "Invalid category ID" });
                }

                var query = new GetProductsQuery 
                { 
                    CategoryId = categoryId,
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
                _logger.LogError(ex, "Exception in GetProductsByCategory for category {CategoryId}", categoryId);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Search products by name
        /// </summary>
        [HttpGet("search")]
        public async Task<IActionResult> SearchProducts([FromQuery] string searchTerm, [FromQuery] int pageNumber = 1, [FromQuery] int pageSize = 10)
        {
            try
            {
                _logger.LogInformation("SearchProducts endpoint called with term: {SearchTerm}", searchTerm);

                if (string.IsNullOrWhiteSpace(searchTerm))
                {
                    return BadRequest(new { error = "Search term is required" });
                }

                var query = new GetProductsQuery 
                { 
                    SearchTerm = searchTerm,
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
                _logger.LogError(ex, "Exception in SearchProducts with term: {SearchTerm}", searchTerm);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Get products with price range filter
        /// </summary>
        [HttpGet("price-range")]
        public async Task<IActionResult> GetProductsByPriceRange([FromQuery] decimal? minPrice, [FromQuery] decimal? maxPrice, [FromQuery] int pageNumber = 1, [FromQuery] int pageSize = 10)
        {
            try
            {
                _logger.LogInformation("GetProductsByPriceRange endpoint called with min: {MinPrice}, max: {MaxPrice}", minPrice, maxPrice);

                if (minPrice.HasValue && maxPrice.HasValue && minPrice > maxPrice)
                {
                    return BadRequest(new { error = "Minimum price cannot be greater than maximum price" });
                }

                var query = new GetProductsQuery 
                { 
                    MinPrice = minPrice,
                    MaxPrice = maxPrice,
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
                _logger.LogError(ex, "Exception in GetProductsByPriceRange with min: {MinPrice}, max: {MaxPrice}", minPrice, maxPrice);
                return StatusCode(500, new { 
                    error = "Internal server error", 
                    details = ex.Message 
                });
            }
        }

        /// <summary>
        /// Health check for products service
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new { 
                status = "GetProducts service is working!", 
                timestamp = DateTime.UtcNow 
            });
        }
    }
}