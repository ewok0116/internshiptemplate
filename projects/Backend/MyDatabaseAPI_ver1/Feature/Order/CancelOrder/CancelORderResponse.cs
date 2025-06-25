
// Features/Orders/CancelOrder/CancelOrderResponse.cs
namespace MyFoodOrderingAPI.Features.Orders.CancelOrder
{
    public class CancelOrderResponse
    {
        public int OrderId { get; set; }
        public int UserId { get; set; }
        public string? Reason { get; set; }
        public DateTime CancelledAt { get; set; }
        public string PreviousStatus { get; set; } = "";
        public bool Success { get; set; }
        public string Message { get; set; } = "";
    }
}
