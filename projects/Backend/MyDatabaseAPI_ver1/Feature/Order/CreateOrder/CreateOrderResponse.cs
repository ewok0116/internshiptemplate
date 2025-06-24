// Features/Orders/CreateOrder/CreateOrderResponse.cs
namespace MyFoodOrderingAPI.Features.Orders.CreateOrder
{
    public class CreateOrderResponse
    {
        public int OrderId { get; set; }
        public string OrderStatus { get; set; } = "";
        public decimal TotalAmount { get; set; }
        public DateTime OrderDate { get; set; }
        public string DeliveryAddress { get; set; } = "";
        public string PaymentMethod { get; set; } = "";
        public List<CreatedOrderItemDto> OrderItems { get; set; } = new();
        public bool Success { get; set; } = true;
        public string Message { get; set; } = "";
    }

    public class CreatedOrderItemDto
    {
        public int Id { get; set; }
        public int ProductId { get; set; }
        public string ProductName { get; set; } = "";
        public int Quantity { get; set; }
        public decimal UnitPrice { get; set; }
        public decimal TotalPrice { get; set; }
    }
}