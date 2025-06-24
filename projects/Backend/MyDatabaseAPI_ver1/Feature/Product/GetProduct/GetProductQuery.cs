
// Features/Products/GetProducts/GetProductsQuery.cs
using MediatR;

namespace MyFoodOrderingAPI.Features.Products.GetProducts
{
    public class GetProductsQuery : IRequest<GetProductsResponse>
    {
        public string? SearchTerm { get; set; }
        public int? CategoryId { get; set; }
        public decimal? MinPrice { get; set; }
        public decimal? MaxPrice { get; set; }
        public int PageNumber { get; set; } = 1;
        public int PageSize { get; set; } = 10;
        public bool? IsAvailable { get; set; } = true; // Changed from IsActive to IsAvailable
    }
}