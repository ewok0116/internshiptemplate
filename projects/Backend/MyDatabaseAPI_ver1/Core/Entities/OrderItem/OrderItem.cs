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

        // 🎯 Constructor for creating new order items
        public OrderItem(int orderId, int productId, int quantity, decimal unitPrice)
        {
            if (orderId <= 0)
                throw new ArgumentException("Order ID must be valid", nameof(orderId));
            if (productId <= 0)
                throw new ArgumentException("Product ID must be valid", nameof(productId));
            if (quantity <= 0)
                throw new ArgumentException("Quantity must be greater than zero", nameof(quantity));
            if (unitPrice <= 0)
                throw new ArgumentException("Unit price must be greater than zero", nameof(unitPrice));

            OrderId = orderId;
            ProductId = productId;
            Quantity = quantity;
            UnitPrice = unitPrice;
            TotalPrice = quantity * unitPrice;
            IsValid = true;
        }

        // 🎯 Constructor for loading from database
        public OrderItem(int id, int orderId, int productId, int quantity, decimal unitPrice)
        {
            Id = id;
            OrderId = orderId;
            ProductId = productId;
            Quantity = quantity;
            UnitPrice = unitPrice;
            TotalPrice = quantity * unitPrice;
            IsValid = true;
        }

        // 🎯 Constructor for loading from database with all fields
        public OrderItem(int id, int orderId, int productId, int quantity, decimal unitPrice, decimal totalPrice)
        {
            Id = id;
            OrderId = orderId;
            ProductId = productId;
            Quantity = quantity;
            UnitPrice = unitPrice;
            TotalPrice = totalPrice;
            
            // Validate if the stored total matches calculation
            var calculatedTotal = Math.Round(quantity * unitPrice, 2);
            IsValid = Math.Abs(totalPrice - calculatedTotal) < 0.01m;
        }

        // 🎯 Business methods
        public void UpdateQuantity(int newQuantity)
        {
            if (newQuantity <= 0)
                throw new ArgumentException("Quantity must be greater than zero");
                
            Quantity = newQuantity;
            TotalPrice = newQuantity * UnitPrice;
        }

        public void UpdatePrice(decimal newUnitPrice)
        {
            if (newUnitPrice <= 0)
                throw new ArgumentException("Unit price must be greater than zero");
                
            UnitPrice = newUnitPrice;
            TotalPrice = Quantity * newUnitPrice;
        }
    }
}