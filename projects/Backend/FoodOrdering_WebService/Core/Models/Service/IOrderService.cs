using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Core.Services
{
    public interface IOrderService
    {
        Task<Order> CreateOrderAsync(CreateOrderRequest request, CancellationToken cancellationToken = default);
        Task<Order?> GetOrderWithDetailsAsync(int orderId, CancellationToken cancellationToken = default);
        Task<decimal> CalculateOrderTotalAsync(List<OrderItemRequest> items, CancellationToken cancellationToken = default);
        Task<bool> ValidateOrderItemsAsync(List<OrderItemRequest> items, CancellationToken cancellationToken = default);
    }
}