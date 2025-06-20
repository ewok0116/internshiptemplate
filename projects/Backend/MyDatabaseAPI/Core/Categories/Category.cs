
// Core/Entities/Category.cs
namespace MyFoodOrderingAPI.Core.Entities
{
    public class Category
    {
        public int Id { get; private set; }
        public string Name { get; private set; }
        public string Description { get; private set; }
        public string ImageUrl { get; private set; }
        public DateTime CreatedAt { get; private set; }

        // Constructor for creating new category
        public Category(string name, string description, string imageUrl = "")
        {
            if (string.IsNullOrWhiteSpace(name))
                throw new ArgumentException("Category name cannot be empty", nameof(name));

            Name = name;
            Description = description ?? "";
            ImageUrl = imageUrl ?? "";
            CreatedAt = DateTime.UtcNow;
        }

        // Constructor for loading from database
        public Category(int id, string name, string description, string imageUrl, DateTime createdAt)
        {
            Id = id;
            Name = name ?? "";
            Description = description ?? "";
            ImageUrl = imageUrl ?? "";
            CreatedAt = createdAt;
        }

        // Business methods
        public void UpdateDetails(string name, string description, string imageUrl = "")
        {
            if (string.IsNullOrWhiteSpace(name))
                throw new ArgumentException("Category name cannot be empty", nameof(name));

            Name = name;
            Description = description ?? "";
            ImageUrl = imageUrl ?? "";
        }
    }
}
