using Microsoft.Data.SqlClient;
using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Infrastructure.Repositories
{
    public class ProductRepository : IProductRepository
    {
        private readonly string _connectionString;

        public ProductRepository(IConfiguration configuration)
        {
            // 🎯 Same connection logic as UserRepository
            _connectionString = GetConnectionString(configuration);
            
            if (string.IsNullOrEmpty(_connectionString))
            {
                throw new InvalidOperationException("Connection string not found in appsettings.json or environment variables.");
            }
        }
 
        private string GetConnectionString(IConfiguration configuration)
        {
            // Try appsettings.json first
            var configConnectionString = configuration.GetConnectionString("DefaultConnection");
            if (!string.IsNullOrEmpty(configConnectionString))
            {
                return configConnectionString;
            }

            // Fallback to environment variables
            var server = Environment.GetEnvironmentVariable("DB_SERVER");
            var database = Environment.GetEnvironmentVariable("DB_NAME");
            var user = Environment.GetEnvironmentVariable("DB_USER");
            var password = Environment.GetEnvironmentVariable("DB_PASSWORD");

            if (!string.IsNullOrEmpty(server) && !string.IsNullOrEmpty(database) && 
                !string.IsNullOrEmpty(user) && !string.IsNullOrEmpty(password))
            {
                return $"Server={server};Database={database};User Id={user};Password={password};TrustServerCertificate=true;Encrypt=false;";
            }

            return null;
        }

        // ✅ FIXED: Updated GetProductsAsync with bool ↔ int conversion
        public async Task<List<Product>> GetProductsAsync(ProductFilter filter, CancellationToken cancellationToken = default)
        {
            var products = new List<Product>();
            var query = BuildGetProductsQuery(filter);

            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            using var command = new SqlCommand(query.Sql, connection);

            foreach (var param in query.Parameters)
            {
                command.Parameters.AddWithValue(param.Key, param.Value);
            }

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            while (await reader.ReadAsync(cancellationToken))
            {
                var product = new Product(
                    id: Convert.ToInt32(reader["PID"]),
                    name: reader["PName"]?.ToString() ?? "",
                    description: reader["PDescription"]?.ToString() ?? "",
                    price: Convert.ToDecimal(reader["Price"]),
                    imageUrl: reader["PImageUrl"]?.ToString() ?? "",
                    categoryId: reader["CID"] != DBNull.Value ? Convert.ToInt32(reader["CID"]) : 0,
                    // ⭐ CHANGE: Convert int (1/0) back to bool for ANSI database
                    isAvailable: reader["IsAvailable"] != DBNull.Value ? Convert.ToInt32(reader["IsAvailable"]) == 1 : true,
                    createdAt: reader["CreatedAt"] != DBNull.Value ? Convert.ToDateTime(reader["CreatedAt"]) : DateTime.UtcNow
                );
                products.Add(product);
            }

            return products;
        }

        // ✅ FIXED: Updated GetProductByIdAsync with bool ↔ int conversion
        public async Task<Product?> GetProductByIdAsync(int id, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "SELECT PID, PName, PDescription, Price, PImageUrl, CID, IsAvailable, CreatedAt FROM Products WHERE PID = @Id";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Id", id);

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            if (await reader.ReadAsync(cancellationToken))
            {
                return new Product(
                    id: Convert.ToInt32(reader["PID"]),
                    name: reader["PName"]?.ToString() ?? "",
                    description: reader["PDescription"]?.ToString() ?? "",
                    price: Convert.ToDecimal(reader["Price"]),
                    imageUrl: reader["PImageUrl"]?.ToString() ?? "",
                    categoryId: reader["CID"] != DBNull.Value ? Convert.ToInt32(reader["CID"]) : 0,
                    // ⭐ CHANGE: Convert int (1/0) back to bool for ANSI database
                    isAvailable: reader["IsAvailable"] != DBNull.Value ? Convert.ToInt32(reader["IsAvailable"]) == 1 : true,
                    createdAt: reader["CreatedAt"] != DBNull.Value ? Convert.ToDateTime(reader["CreatedAt"]) : DateTime.UtcNow
                );
            }

            return null;
        }

        // ✅ NEW: Added missing AddProductAsync method
        public async Task<Product> AddProductAsync(Product product, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            // ⭐ ANSI-compliant: Use CURRENT_TIMESTAMP and separate INSERT/SELECT
            var sql = @"INSERT INTO Products (PName, PDescription, Price, PImageUrl, CID, IsAvailable, CreatedAt) 
                       VALUES (@Name, @Description, @Price, @ImageUrl, @CategoryId, @IsAvailable, CURRENT_TIMESTAMP);
                       SELECT PID, PName, PDescription, Price, PImageUrl, CID, IsAvailable, CreatedAt 
                       FROM Products WHERE PID = SCOPE_IDENTITY();";
            
            using var command = new SqlCommand(sql, connection);
            
            command.Parameters.AddWithValue("@Name", product.Name);
            command.Parameters.AddWithValue("@Description", product.Description);
            command.Parameters.AddWithValue("@Price", product.Price);
            command.Parameters.AddWithValue("@ImageUrl", product.ImageUrl);
            command.Parameters.AddWithValue("@CategoryId", product.CategoryId);
            // ⭐ CHANGE: Convert bool to int (1/0) for ANSI database
            command.Parameters.AddWithValue("@IsAvailable", product.IsAvailable ? 1 : 0);

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            if (await reader.ReadAsync(cancellationToken))
            {
                return new Product(
                    id: Convert.ToInt32(reader["PID"]),
                    name: reader["PName"]?.ToString() ?? "",
                    description: reader["PDescription"]?.ToString() ?? "",
                    price: Convert.ToDecimal(reader["Price"]),
                    imageUrl: reader["PImageUrl"]?.ToString() ?? "",
                    categoryId: reader["CID"] != DBNull.Value ? Convert.ToInt32(reader["CID"]) : 0,
                    // ⭐ CHANGE: Convert int (1/0) back to bool
                    isAvailable: reader["IsAvailable"] != DBNull.Value ? Convert.ToInt32(reader["IsAvailable"]) == 1 : true,
                    createdAt: reader["CreatedAt"] != DBNull.Value ? Convert.ToDateTime(reader["CreatedAt"]) : DateTime.UtcNow
                );
            }

            throw new InvalidOperationException("Failed to insert product");
        }

        // ✅ NEW: Added missing UpdateProductAsync method
        public async Task<Product> UpdateProductAsync(Product product, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = @"UPDATE Products 
                       SET PName = @Name, PDescription = @Description, Price = @Price, 
                           PImageUrl = @ImageUrl, CID = @CategoryId, IsAvailable = @IsAvailable 
                       WHERE PID = @Id";
            using var command = new SqlCommand(sql, connection);
            
            command.Parameters.AddWithValue("@Id", product.Id);
            command.Parameters.AddWithValue("@Name", product.Name);
            command.Parameters.AddWithValue("@Description", product.Description);
            command.Parameters.AddWithValue("@Price", product.Price);
            command.Parameters.AddWithValue("@ImageUrl", product.ImageUrl);
            command.Parameters.AddWithValue("@CategoryId", product.CategoryId);
            // ⭐ CHANGE: Convert bool to int (1/0) for ANSI database
            command.Parameters.AddWithValue("@IsAvailable", product.IsAvailable ? 1 : 0);

            await command.ExecuteNonQueryAsync(cancellationToken);
            return product;
        }

        // ✅ NEW: Added missing DeleteProductAsync method
        public async Task<bool> DeleteProductAsync(int id, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "DELETE FROM Products WHERE PID = @Id";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Id", id);

            var rowsAffected = await command.ExecuteNonQueryAsync(cancellationToken);
            return rowsAffected > 0;
        }

        public async Task<int> GetProductsCountAsync(ProductFilter filter, CancellationToken cancellationToken = default)
        {
            var query = BuildCountQuery(filter);

            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            using var command = new SqlCommand(query.Sql, connection);

            foreach (var param in query.Parameters)
            {
                command.Parameters.AddWithValue(param.Key, param.Value);
            }

            var result = await command.ExecuteScalarAsync(cancellationToken);
            return Convert.ToInt32(result);
        }

        public async Task<List<Product>> GetProductsByCategoryAsync(int categoryId, CancellationToken cancellationToken = default)
        {
            var filter = new ProductFilter { CategoryId = categoryId };
            return await GetProductsAsync(filter, cancellationToken);
        }

        // ✅ NEW: Added method to get available products only
        public async Task<List<Product>> GetAvailableProductsAsync(CancellationToken cancellationToken = default)
        {
            var filter = new ProductFilter { IsAvailable = true };
            return await GetProductsAsync(filter, cancellationToken);
        }

        // ✅ FIXED: Updated BuildGetProductsQuery with bool ↔ int conversion
        private (string Sql, Dictionary<string, object> Parameters) BuildGetProductsQuery(ProductFilter filter)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            var sql = "SELECT PID, PName, PDescription, Price, PImageUrl, CID, IsAvailable, CreatedAt FROM Products";

            if (!string.IsNullOrWhiteSpace(filter.SearchTerm))
            {
                conditions.Add("(PName LIKE @SearchTerm OR PDescription LIKE @SearchTerm)");
                parameters["@SearchTerm"] = $"%{filter.SearchTerm}%";
            }

            if (filter.CategoryId.HasValue)
            {
                conditions.Add("CID = @CategoryId");
                parameters["@CategoryId"] = filter.CategoryId.Value;
            }

            if (filter.MinPrice.HasValue)
            {
                conditions.Add("Price >= @MinPrice");
                parameters["@MinPrice"] = filter.MinPrice.Value;
            }

            if (filter.MaxPrice.HasValue)
            {
                conditions.Add("Price <= @MaxPrice");
                parameters["@MaxPrice"] = filter.MaxPrice.Value;
            }

            if (filter.IsAvailable.HasValue)
            {
                conditions.Add("IsAvailable = @IsAvailable");
                // ⭐ CHANGE: Convert bool to int (1/0) for database parameter
                parameters["@IsAvailable"] = filter.IsAvailable.Value ? 1 : 0;
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            sql += " ORDER BY PName";

            var offset = (filter.PageNumber - 1) * filter.PageSize;
            sql += $" OFFSET {offset} ROWS FETCH NEXT {filter.PageSize} ROWS ONLY";

            return (sql, parameters);
        }

        // ✅ FIXED: Updated BuildCountQuery with bool ↔ int conversion
        private (string Sql, Dictionary<string, object> Parameters) BuildCountQuery(ProductFilter filter)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            var sql = "SELECT COUNT(*) FROM Products";

            if (!string.IsNullOrWhiteSpace(filter.SearchTerm))
            {
                conditions.Add("(PName LIKE @SearchTerm OR PDescription LIKE @SearchTerm)");
                parameters["@SearchTerm"] = $"%{filter.SearchTerm}%";
            }

            if (filter.CategoryId.HasValue)
            {
                conditions.Add("CID = @CategoryId");
                parameters["@CategoryId"] = filter.CategoryId.Value;
            }

            if (filter.MinPrice.HasValue)
            {
                conditions.Add("Price >= @MinPrice");
                parameters["@MinPrice"] = filter.MinPrice.Value;
            }

            if (filter.MaxPrice.HasValue)
            {
                conditions.Add("Price <= @MaxPrice");
                parameters["@MaxPrice"] = filter.MaxPrice.Value;
            }

            if (filter.IsAvailable.HasValue)
            {
                conditions.Add("IsAvailable = @IsAvailable");
                // ⭐ CHANGE: Convert bool to int (1/0) for database parameter
                parameters["@IsAvailable"] = filter.IsAvailable.Value ? 1 : 0;
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            return (sql, parameters);
        }
    }
}