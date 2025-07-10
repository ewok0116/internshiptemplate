// Features/OrderItems/GetOrderItems/GetOrderItemsResponse.cs
namespace MyFoodOrderingAPI.Features.OrderItems.GetOrderItems
{
    public class GetOrderItemsResponse
    {
        public List<OrderItemDetailDto> OrderItems { get; set; } = new();
        public decimal TotalAmount { get; set; }
        public int TotalItems { get; set; }
        public bool Success { get; set; }
        public string Message { get; set; } = "";
    }

    public class GetOrderItemResponse
    {
        public OrderItemDetailDto? OrderItem { get; set; }
        public bool Success { get; set; }
        public string Message { get; set; } = "";
    }

    public class OrderItemDetailDto
    {
        public int Id { get; set; }
        public int OrderId { get; set; }
        public int ProductId { get; set; }
        public string ProductName { get; set; } = "";
        public string ProductDescription { get; set; } = "";
        public string ProductImageUrl { get; set; } = "";
        public int Quantity { get; set; }
        public decimal UnitPrice { get; set; }
        public decimal TotalPrice { get; set; }
        public bool IsValid { get; set; }
    }
}