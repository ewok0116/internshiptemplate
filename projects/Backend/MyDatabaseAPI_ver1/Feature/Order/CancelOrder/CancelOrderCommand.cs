// Features/Orders/CancelOrder/CancelOrderCommand.cs
using MediatR;

namespace MyFoodOrderingAPI.Features.Orders.CancelOrder
{
    public class CancelOrderCommand : IRequest<CancelOrderResponse>
    {
        public int OrderId { get; set; }
        public int UserId { get; set; }
        public string? Reason { get; set; }
    }
}