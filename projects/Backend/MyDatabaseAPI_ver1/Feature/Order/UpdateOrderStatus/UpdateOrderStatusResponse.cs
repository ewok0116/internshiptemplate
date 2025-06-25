
// Features/Orders/UpdateOrderStatus/UpdateOrderStatusResponse.cs
namespace MyFoodOrderingAPI.Features.Orders.UpdateOrderStatus
{
    public class UpdateOrderStatusResponse
    {
        public int OrderId { get; set; }
        public string OldStatus { get; set; } = "";
        public string NewStatus { get; set; } = "";
        public DateTime UpdatedAt { get; set; }
        public bool Success { get; set; }
        public string Message { get; set; } = "";
    }

    // ❌ REMOVE StatusUpdateRequest from here - it's only needed in controller
}
