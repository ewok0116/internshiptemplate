using MediatR;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Features.Products.GetProducts
{
    public class GetProductsHandler : IRequestHandler<GetProductsQuery, GetProductsResponse>
    {
        // 🎯 DI - Inject repository instead of IConfiguration
        private readonly IProductRepository _productRepository;
        private readonly ILogger<GetProductsHandler> _logger;

        public GetProductsHandler(
            IProductRepository productRepository,
            ILogger<GetProductsHandler> logger)
        {
            _productRepository = productRepository;
            _logger = logger;
        }

        public async Task<GetProductsResponse> Handle(GetProductsQuery request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("Retrieving products with search term: {SearchTerm}, category: {CategoryId}", 
                    request.SearchTerm, request.CategoryId);

                // 🎯 Create filter from request
                var filter = new ProductFilter
                {
                    SearchTerm = request.SearchTerm,
                    CategoryId = request.CategoryId,
                    MinPrice = request.MinPrice,
                    MaxPrice = request.MaxPrice,
                    IsAvailable = request.IsAvailable,
                    PageNumber = 1, // You can add pagination to GetProductsQuery if needed
                    PageSize = 100  // Or use request parameters
                };

                // 🎯 Use repository instead of direct SQL
                var productEntities = await _productRepository.GetProductsAsync(filter, cancellationToken);
                var totalCount = await _productRepository.GetProductsCountAsync(filter, cancellationToken);

                // 🎯 Same DTO mapping as before
                var productDtos = productEntities.Select(entity => new ProductDto
                {
                    Id = entity.Id,
                    Name = entity.Name,
                    Description = entity.Description,
                    Price = entity.Price,
                    ImageUrl = entity.ImageUrl,
                    CategoryId = entity.CategoryId,
                    IsAvailable = entity.IsAvailable,
                    CreatedAt = entity.CreatedAt
                }).ToList();

                _logger.LogInformation("Successfully retrieved {Count} products out of {TotalCount} total", 
                    productDtos.Count, totalCount);

                return new GetProductsResponse
                {
                    Data = productDtos,
                    TotalCount = totalCount, // 🎯 Now using actual count from database
                    Success = true,
                    Message = "Products retrieved successfully"
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error retrieving products with search term: {SearchTerm}", request.SearchTerm);
                return new GetProductsResponse
                {
                    Success = false,
                    Message = "An error occurred while retrieving products"
                };
            }
        }

        // 🗑️ REMOVED - GetProductsFromDatabaseAsync method
        // 🗑️ REMOVED - BuildSqlQuery method
        // This logic is now in ProductRepository!
    }
}