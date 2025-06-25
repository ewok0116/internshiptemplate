using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Core.Interfaces
{
    public interface IOrderItemRepository
    {
        Task<List<OrderItem>> GetOrderItemsAsync(OrderItemFilter filter, CancellationToken cancellationToken = default);
        Task<OrderItem?> GetOrderItemByIdAsync(int id, CancellationToken cancellationToken = default);
        Task<List<OrderItem>> GetOrderItemsByOrderIdAsync(int orderId, CancellationToken cancellationToken = default);
        Task<List<OrderItem>> GetOrderItemsByProductIdAsync(int productId, CancellationToken cancellationToken = default);
        Task<OrderItem> AddOrderItemAsync(OrderItem orderItem, CancellationToken cancellationToken = default);
        Task<OrderItem> UpdateOrderItemAsync(OrderItem orderItem, CancellationToken cancellationToken = default);
        Task<bool> DeleteOrderItemAsync(int id, CancellationToken cancellationToken = default);
        Task<bool> DeleteOrderItemsByOrderIdAsync(int orderId, CancellationToken cancellationToken = default);
        Task<int> GetOrderItemsCountAsync(OrderItemFilter filter, CancellationToken cancellationToken = default);
        Task<decimal> GetOrderTotalAsync(int orderId, CancellationToken cancellationToken = default);
    }
}