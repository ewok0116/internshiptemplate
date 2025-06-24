// Features/OrderItems/GetOrderItems/GetOrderItemsQuery.cs
using MediatR;

namespace MyFoodOrderingAPI.Features.OrderItems.GetOrderItems
{
    public class GetOrderItemsQuery : IRequest<GetOrderItemsResponse>
    {
        public int OrderId { get; set; }
    }

    public class GetOrderItemQuery : IRequest<GetOrderItemResponse>
    {
        public int OrderId { get; set; }
        public int ItemId { get; set; }
    }
}