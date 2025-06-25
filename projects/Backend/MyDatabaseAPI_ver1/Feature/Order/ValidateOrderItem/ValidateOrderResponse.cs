
// Features/Orders/ValidateOrderItems/ValidateOrderItemsResponse.cs
namespace MyFoodOrderingAPI.Features.Orders.ValidateOrderItems
{
    public class ValidateOrderItemsResponse
    {
        public bool IsValid { get; set; }
        public List<ItemValidationResult> ValidationResults { get; set; } = new();
        public List<string> Errors { get; set; } = new();
        public List<string> Warnings { get; set; } = new();
        public int ValidItemCount { get; set; }
        public int InvalidItemCount { get; set; }
        public bool Success { get; set; }
        public string Message { get; set; } = "";
    }

    public class ItemValidationResult
    {
        public int ProductId { get; set; }
        public string ProductName { get; set; } = "";
        public int RequestedQuantity { get; set; }
        public int AvailableStock { get; set; }
        public decimal UnitPrice { get; set; }
        public bool ProductExists { get; set; }
        public bool IsAvailable { get; set; }
        public bool HasSufficientStock { get; set; }
        public bool IsValid { get; set; }
        public List<string> Issues { get; set; } = new();
    }
}
