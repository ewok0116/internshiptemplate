
// =============================================================================
// UPDATED: Product Entity - Matching Your Database
// =============================================================================

// Core/Entities/Product.cs
namespace MyFoodOrderingAPI.Core.Entities
{
    public class Product
    {
        public int Id { get; private set; }
        public string Name { get; private set; }
        public string Description { get; private set; }
        public decimal Price { get; private set; }
        public string ImageUrl { get; private set; }
        public int CategoryId { get; private set; }
        public bool IsAvailable { get; private set; }
        public DateTime CreatedAt { get; private set; }

        // Constructor for creating new product
        public Product(string name, string description, decimal price, string imageUrl, int categoryId)
        {
            if (string.IsNullOrWhiteSpace(name))
                throw new ArgumentException("Product name cannot be empty", nameof(name));
            
            if (price <= 0)
                throw new ArgumentException("Product price must be greater than zero", nameof(price));

            Name = name;
            Description = description ?? "";
            Price = price;
            ImageUrl = imageUrl ?? "";
            CategoryId = categoryId;
            IsAvailable = true; // Default to available
            CreatedAt = DateTime.UtcNow;
        }

        // Constructor for loading from database
        public Product(int id, string name, string description, decimal price, string imageUrl, int categoryId, bool isAvailable, DateTime createdAt)
        {
            Id = id;
            Name = name ?? "";
            Description = description ?? "";
            Price = price;
            ImageUrl = imageUrl ?? "";
            CategoryId = categoryId;
            IsAvailable = isAvailable;
            CreatedAt = createdAt;
        }

        // Business methods
        public void UpdateDetails(string name, string description, decimal price, string imageUrl)
        {
            if (string.IsNullOrWhiteSpace(name))
                throw new ArgumentException("Product name cannot be empty", nameof(name));
            
            if (price <= 0)
                throw new ArgumentException("Product price must be greater than zero", nameof(price));

            Name = name;
            Description = description ?? "";
            Price = price;
            ImageUrl = imageUrl ?? "";
        }

        public void MakeUnavailable()
        {
            IsAvailable = false;
        }

        public void MakeAvailable()
        {
            IsAvailable = true;
        }

        public bool CanBeOrdered()
        {
            return IsAvailable;
        }
    }
}
