// Features/Orders/UpdateOrderStatus/UpdateOrderStatusCommand.cs
using MediatR;

namespace MyFoodOrderingAPI.Features.Orders.UpdateOrderStatus
{
    public class UpdateOrderStatusCommand : IRequest<UpdateOrderStatusResponse>
    {
        public int OrderId { get; set; }
        public string NewStatus { get; set; } = "";
    }
}
