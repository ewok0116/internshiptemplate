
// Features/Categories/GetCategories/GetCategoriesQuery.cs
using MediatR;

namespace MyFoodOrderingAPI.Features.Categories.GetCategories
{
    public class GetCategoriesQuery : IRequest<GetCategoriesResponse>
    {
        public string? SearchTerm { get; set; }
        public int PageNumber { get; set; } = 1;
        public int PageSize { get; set; } = 10;
    }
}
