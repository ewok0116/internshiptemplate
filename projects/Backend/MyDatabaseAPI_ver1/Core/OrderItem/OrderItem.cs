
// Core/Entities/OrderItem.cs
namespace MyFoodOrderingAPI.Core.Entities
{
    public class OrderItem
    {
        public int Id { get; private set; }
        public int OrderId { get; private set; }
        public int ProductId { get; private set; }
        public int Quantity { get; private set; }
        public decimal UnitPrice { get; private set; }
        public decimal TotalPrice { get; set; }
        public bool IsValid { get; set; }
    }
}
