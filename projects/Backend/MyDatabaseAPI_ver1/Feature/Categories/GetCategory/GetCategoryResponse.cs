
// Features/Categories/GetCategories/GetCategoriesResponse.cs
namespace MyFoodOrderingAPI.Features.Categories.GetCategories
{
    public class GetCategoriesResponse
    {
        public List<CategoryDto> Data { get; set; } = new();
        public int TotalCount { get; set; }
        public bool Success { get; set; } = true;
        public string Message { get; set; } = "";
    }

    public class CategoryDto
    {
        public int Id { get; set; }
        public string Name { get; set; } = "";
        public string Description { get; set; } = "";
        public string ImageUrl { get; set; } = "";
        public DateTime CreatedAt { get; set; }
        public int ProductCount { get; set; } // Bonus: number of products in this category
    }
}