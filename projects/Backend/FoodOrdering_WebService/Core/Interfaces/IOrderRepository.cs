using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Core.Interfaces
{
    public interface IOrderRepository
    {
        Task<List<Order>> GetOrdersAsync(OrderFilter filter, CancellationToken cancellationToken = default);
        Task<Order?> GetOrderByIdAsync(int id, CancellationToken cancellationToken = default);
        Task<Order?> GetOrderWithItemsAsync(int id, CancellationToken cancellationToken = default);
        Task<List<Order>> GetOrdersByUserIdAsync(int userId, CancellationToken cancellationToken = default);
        Task<Order> AddOrderAsync(Order order, CancellationToken cancellationToken = default);
        Task<Order> UpdateOrderAsync(Order order, CancellationToken cancellationToken = default);
        Task<bool> DeleteOrderAsync(int id, CancellationToken cancellationToken = default);
        Task<int> GetOrdersCountAsync(OrderFilter filter, CancellationToken cancellationToken = default);
        Task<List<Order>> GetOrdersByDateRangeAsync(DateTime startDate, DateTime endDate, CancellationToken cancellationToken = default);
    }
}