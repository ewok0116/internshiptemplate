// Features/Orders/CreateOrder/CreateOrderCommand.cs
using MediatR;

namespace MyFoodOrderingAPI.Features.Orders.CreateOrder
{
    public class CreateOrderCommand : IRequest<CreateOrderResponse>
    {
        public int UserId { get; set; }
        public string DeliveryAddress { get; set; } = "";
        public string PaymentMethod { get; set; } = "";
        public List<CreateOrderItemDto> OrderItems { get; set; } = new();
    }

    public class CreateOrderItemDto
    {
        public int ProductId { get; set; }
        public int Quantity { get; set; }
    }
}