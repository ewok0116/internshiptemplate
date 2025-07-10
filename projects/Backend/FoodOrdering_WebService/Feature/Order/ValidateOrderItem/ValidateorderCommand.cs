// Features/Orders/ValidateOrderItems/ValidateOrderItemsCommand.cs
using MediatR;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Features.Orders.ValidateOrderItems
{
    public class ValidateOrderItemsCommand : IRequest<ValidateOrderItemsResponse>
    {
        public List<OrderItemRequest> Items { get; set; } = new();
        public bool CheckStock { get; set; } = true;
        public bool CheckAvailability { get; set; } = true;
    }
}
