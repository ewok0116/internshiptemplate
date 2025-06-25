// Features/Categories/GetCategories/GetCategoriesHandler.cs
using MediatR;
using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Models;
using System.Threading;
using System.Threading.Tasks;

namespace MyFoodOrderingAPI.Features.Categories.GetCategories
{
    public class GetCategoriesHandler : IRequestHandler<GetCategoriesQuery, GetCategoriesResponse>
    {
        private readonly ICategoryRepository _categoryRepository;

        public GetCategoriesHandler(ICategoryRepository categoryRepository)
        {
            _categoryRepository = categoryRepository;
        }

        public async Task<GetCategoriesResponse> Handle(GetCategoriesQuery request, CancellationToken cancellationToken)
        {
            try
            {
                // Create filter from query
                var filter = new CategoryFilter
                {
                    SearchTerm = request.SearchTerm,
                    PageNumber = request.PageNumber,
                    PageSize = request.PageSize
                };

                // Get categories and total count
                var categories = await _categoryRepository.GetCategoriesAsync(filter, cancellationToken);
                var totalCount = await _categoryRepository.GetCategoriesCountAsync(filter, cancellationToken);

                // Map to DTOs
                var categoryDtos = categories.Select(c => new CategoryDto
                {
                    Id = c.Id,
                    Name = c.Name,
                    Description = c.Description,
                    ImageUrl = c.ImageUrl,
                    CreatedAt = c.CreatedAt,
                    ProductCount = 0 // This would need to be populated from a product repository
                }).ToList();

                return new GetCategoriesResponse
                {
                    Data = categoryDtos,
                    TotalCount = totalCount,
                    Success = true,
                    Message = "Categories retrieved successfully"
                };
            }
            catch (Exception ex)
            {
                return new GetCategoriesResponse
                {
                    Success = false,
                    Message = $"An error occurred while retrieving categories: {ex.Message}"
                };
            }
        }
    }
}