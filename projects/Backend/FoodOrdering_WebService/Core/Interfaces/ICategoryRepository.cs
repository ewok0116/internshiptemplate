using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Models; 


namespace MyFoodOrderingAPI.Core.Interfaces
{
    public interface ICategoryRepository
    {
        Task<List<Categories>> GetCategoriesAsync(CategoryFilter filter, CancellationToken cancellationToken = default);
        Task<Categories?> GetCategoryByIdAsync(int id, CancellationToken cancellationToken = default);
        Task<Categories> AddCategoryAsync(Categories category, CancellationToken cancellationToken = default);
        Task<Categories> UpdateCategoryAsync(Categories category, CancellationToken cancellationToken = default);
        Task<bool> DeleteCategoryAsync(int id, CancellationToken cancellationToken = default);
        Task<int> GetCategoriesCountAsync(CategoryFilter filter, CancellationToken cancellationToken = default);
        Task<List<Categories>> GetActiveCategoriesAsync(CancellationToken cancellationToken = default);
    }
}