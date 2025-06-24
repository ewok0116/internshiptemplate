// Features/Orders/GetOrders/GetOrdersQuery.cs
using MediatR;

namespace MyFoodOrderingAPI.Features.Orders.GetOrders
{
    public class GetOrdersQuery : IRequest<GetOrdersResponse>
    {
        public int? UserId { get; set; }
        public string? Status { get; set; }
        public int PageNumber { get; set; } = 1;
        public int PageSize { get; set; } = 10;
    }

    public class GetOrderByIdQuery : IRequest<GetOrderByIdResponse>
    {
        public int OrderId { get; set; }
    }
}