
// Features/Products/GetProducts/GetProductsResponse.cs
namespace MyFoodOrderingAPI.Features.Products.GetProducts
{
    public class GetProductsResponse
    {
        public List<ProductDto> Data { get; set; } = new();
        public int TotalCount { get; set; }
        public bool Success { get; set; } = true;
        public string Message { get; set; } = "";
    }

    public class ProductDto
    {
        public int Id { get; set; }
        public string Name { get; set; } = "";
        public string Description { get; set; } = "";
        public decimal Price { get; set; }
        public string ImageUrl { get; set; } = "";
        public int CategoryId { get; set; }
        public bool IsAvailable { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}
