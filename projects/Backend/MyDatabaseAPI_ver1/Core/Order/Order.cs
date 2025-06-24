
// Core/Entities/Order.cs
namespace MyFoodOrderingAPI.Core.Entities
{
    public class Order
    {
        public int Id { get; private set; }
        public int UserId { get; private set; }
        public string OrderStatus { get; private set; }
        public decimal TotalAmount { get; private set; }
        public string DeliveryAddress { get; private set; }
        public DateTime OrderDate { get; private set; }
        public string PaymentMethod { get; private set; }
        public List<OrderItem> OrderItems { get; private set; } = new();

        public Order(int userId, decimal totalAmount, string deliveryAddress, string paymentMethod)
        {
            if (userId <= 0)
                throw new ArgumentException("User ID must be valid", nameof(userId));
            if (totalAmount <= 0)
                throw new ArgumentException("Total amount must be greater than zero", nameof(totalAmount));
            if (string.IsNullOrWhiteSpace(deliveryAddress))
                throw new ArgumentException("Delivery address is required", nameof(deliveryAddress));
            if (string.IsNullOrWhiteSpace(paymentMethod))
                throw new ArgumentException("Payment method is required", nameof(paymentMethod));

            UserId = userId;
            TotalAmount = totalAmount;
            DeliveryAddress = deliveryAddress;
            PaymentMethod = paymentMethod;
            OrderStatus = "Pending";
            OrderDate = DateTime.UtcNow;
        }

        public Order(int id, int userId, string orderStatus, decimal totalAmount, string deliveryAddress, DateTime orderDate, string paymentMethod)
        {
            Id = id;
            UserId = userId;
            OrderStatus = orderStatus ?? "Pending";
            TotalAmount = totalAmount;
            DeliveryAddress = deliveryAddress ?? "";
            OrderDate = orderDate;
            PaymentMethod = paymentMethod ?? "";
        }

        public void UpdateStatus(string newStatus)
        {
            var validStatuses = new[] { "Pending", "Confirmed", "Preparing", "Ready", "Delivered", "Cancelled" };
            if (!validStatuses.Contains(newStatus))
                throw new ArgumentException($"Invalid order status: {newStatus}");

            OrderStatus = newStatus;
        }

        public void AddOrderItem(OrderItem item)
        {
            OrderItems.Add(item);
        }

        public bool CanBeCancelled()
        {
            return OrderStatus == "Pending" || OrderStatus == "Confirmed";
        }

        public void ConfirmOrder()
        {
            if (OrderStatus == "Pending")
                OrderStatus = "Confirmed";
        }
    }
}
