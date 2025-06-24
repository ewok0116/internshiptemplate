// Features/Orders/GetOrders/GetOrdersResponse.cs
namespace MyFoodOrderingAPI.Features.Orders.GetOrders
{
    public class GetOrdersResponse
    {
        public List<OrderSummaryDto> Orders { get; set; } = new();
        public int TotalCount { get; set; }
        public bool Success { get; set; } = true;
        public string Message { get; set; } = "";
    }

    public class GetOrderByIdResponse
    {
        public OrderDetailDto? Order { get; set; }
        public bool Success { get; set; } = true;
        public string Message { get; set; } = "";
    }

    public class OrderSummaryDto
    {
        public int Id { get; set; }
        public int UserId { get; set; }
        public string UserName { get; set; } = "";
        public string OrderStatus { get; set; } = "";
        public decimal TotalAmount { get; set; }
        public string DeliveryAddress { get; set; } = "";
        public DateTime OrderDate { get; set; }
        public string PaymentMethod { get; set; } = "";
        public int ItemCount { get; set; }
    }

    public class OrderDetailDto
    {
        public int Id { get; set; }
        public int UserId { get; set; }
        public string UserName { get; set; } = "";
        public string OrderStatus { get; set; } = "";
        public decimal TotalAmount { get; set; }
        public string DeliveryAddress { get; set; } = "";
        public DateTime OrderDate { get; set; }
        public string PaymentMethod { get; set; } = "";
        public List<OrderItemDetailDto> OrderItems { get; set; } = new();
    }

    public class OrderItemDetailDto
    {
        public int Id { get; set; }
        public int ProductId { get; set; }
        public string ProductName { get; set; } = "";
        public int Quantity { get; set; }
        public decimal UnitPrice { get; set; }
        public decimal TotalPrice { get; set; }
    }
}