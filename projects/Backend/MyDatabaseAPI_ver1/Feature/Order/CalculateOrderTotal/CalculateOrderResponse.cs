
// Features/Orders/CalculateOrderTotal/CalculateOrderTotalResponse.cs
namespace MyFoodOrderingAPI.Features.Orders.CalculateOrderTotal
{
    public class CalculateOrderTotalResponse
    {
        public List<CalculatedItemDto> Items { get; set; } = new();
        public decimal SubTotal { get; set; }
        public decimal TaxAmount { get; set; }
        public decimal DeliveryFee { get; set; }
        public decimal DiscountAmount { get; set; }
        public decimal GrandTotal { get; set; }
        public string? CouponCode { get; set; }
        public bool CouponApplied { get; set; }
        public bool Success { get; set; }
        public string Message { get; set; } = "";
    }

    public class CalculatedItemDto
    {
        public int ProductId { get; set; }
        public string ProductName { get; set; } = "";
        public int Quantity { get; set; }
        public decimal UnitPrice { get; set; }
        public decimal TotalPrice { get; set; }
        public bool IsAvailable { get; set; }
    }
}
