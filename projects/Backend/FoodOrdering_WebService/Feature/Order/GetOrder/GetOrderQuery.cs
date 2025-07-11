// Features/Orders/GetOrders/GetOrdersQuery.cs
using MediatR;

namespace MyFoodOrderingAPI.Features.Orders.GetOrders
{
    public class GetOrdersQuery : IRequest<GetOrdersResponse>
    {
        public int? UserId { get; set; }
        //public string? Status { get; set; }
        public int PageNumber { get; set; } = 1;
        public int PageSize { get; set; } = 10;
        public DateTime? OrderDateFrom { get; set; }
        public DateTime? OrderDateTo { get; set; }
        public decimal? MinAmount { get; set; }
        public decimal? MaxAmount { get; set; }
        public string? PaymentMethod { get; set; }
    }

    public class GetOrderByIdQuery : IRequest<GetOrderByIdResponse>
    {
        public int OrderId { get; set; }
    }
}
