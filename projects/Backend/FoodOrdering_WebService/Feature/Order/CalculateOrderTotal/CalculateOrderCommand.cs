// Features/Orders/CalculateOrderTotal/CalculateOrderTotalCommand.cs
using MediatR;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Features.Orders.CalculateOrderTotal
{
    public class CalculateOrderTotalCommand : IRequest<CalculateOrderTotalResponse>
    {
        public List<OrderItemRequest> Items { get; set; } = new();
        public string? CouponCode { get; set; }
        public decimal? TaxRate { get; set; }
        public decimal? DeliveryFee { get; set; }
    }
}