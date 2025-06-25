using System.ComponentModel.DataAnnotations;

namespace MyFoodOrderingAPI.Core.Models
{
    // 🎯 Request models for Order operations
    public class CreateOrderRequest
    {
        [Required]
        [Range(1, int.MaxValue, ErrorMessage = "User ID must be valid")]
        public int UserId { get; set; }

        [Required]
        [StringLength(500, ErrorMessage = "Delivery address cannot exceed 500 characters")]
        public string DeliveryAddress { get; set; } = "";

        [Required]
        [StringLength(100, ErrorMessage = "Payment method cannot exceed 100 characters")]
        public string PaymentMethod { get; set; } = "";

        [Required]
        [MinLength(1, ErrorMessage = "At least one item is required")]
        public List<OrderItemRequest> Items { get; set; } = new();

        // Business validation
        public bool IsValid()
        {
            return UserId > 0 
                && !string.IsNullOrWhiteSpace(DeliveryAddress)
                && !string.IsNullOrWhiteSpace(PaymentMethod)
                && Items.Any()
                && Items.All(item => item.IsValid());
        }
    }

    public class OrderItemRequest
    {
        [Required]
        [Range(1, int.MaxValue, ErrorMessage = "Product ID must be valid")]
        public int ProductId { get; set; }

        [Required]
        [Range(1, 100, ErrorMessage = "Quantity must be between 1 and 100")]
        public int Quantity { get; set; }

        // Business validation
        public bool IsValid()
        {
            return ProductId > 0 && Quantity > 0 && Quantity <= 100;
        }
    }

    // 🎯 Request models for updating orders
    public class UpdateOrderStatusRequest
    {
        [Required]
        public string NewStatus { get; set; } = "";

        public bool IsValid()
        {
            var validStatuses = new[] { "Pending", "Confirmed", "Preparing", "Ready", "Delivered", "Cancelled" };
            return validStatuses.Contains(NewStatus);
        }
    }

    public class CancelOrderRequest
    {
        [Required]
        [Range(1, int.MaxValue, ErrorMessage = "User ID must be valid")]
        public int UserId { get; set; }

        public string? Reason { get; set; }
    }
}