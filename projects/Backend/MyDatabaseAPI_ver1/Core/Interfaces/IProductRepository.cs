using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Models; 
namespace MyFoodOrderingAPI.Core.Interfaces
{
    public interface IProductRepository
    {
        Task<List<Product>> GetProductsAsync(ProductFilter filter, CancellationToken cancellationToken = default);
        Task<Product?> GetProductByIdAsync(int id, CancellationToken cancellationToken = default);
        Task<int> GetProductsCountAsync(ProductFilter filter, CancellationToken cancellationToken = default);
        Task<List<Product>> GetProductsByCategoryAsync(int categoryId, CancellationToken cancellationToken = default);
    }
}