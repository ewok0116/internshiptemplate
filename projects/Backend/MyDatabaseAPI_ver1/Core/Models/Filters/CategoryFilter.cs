// Core/Models/CategoryFilter.cs
namespace MyFoodOrderingAPI.Core.Models
{
    public class CategoryFilter
    {
        public string? SearchTerm { get; set; }
        public bool? IsActive { get; set; }
        public DateTime? CreatedAfter { get; set; }
        public DateTime? CreatedBefore { get; set; }
        public int PageNumber { get; set; } = 1;
        public int PageSize { get; set; } = 10;
    }
}