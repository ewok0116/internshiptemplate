using Microsoft.Data.SqlClient;
using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Infrastructure.Repositories
{
    public class CategoryRepository : ICategoryRepository
    {
        private readonly string _connectionString;

        public CategoryRepository(IConfiguration configuration)
        {
            // 🎯 Same connection logic as other repositories
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

        public async Task<List<Categories>> GetCategoriesAsync(CategoryFilter filter, CancellationToken cancellationToken = default)
        {
            var categories = new List<Categories>();
            var query = BuildGetCategoriesQuery(filter);

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
                // 🎯 Updated to match your Categories entity constructor
                var category = new Categories(
                    id: Convert.ToInt32(reader["CID"]),
                    name: reader["CName"]?.ToString() ?? "",
                    description: reader["CDescription"]?.ToString() ?? "",
                    imageUrl: reader["ImageUrl"]?.ToString() ?? "", // Added imageUrl parameter
                    createdAt: reader["CreatedAt"] != DBNull.Value ? Convert.ToDateTime(reader["CreatedAt"]) : DateTime.UtcNow
                );
                categories.Add(category);
            }

            return categories;
        }

        public async Task<Categories?> GetCategoryByIdAsync(int id, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "SELECT CID, CName, CDescription, ImageUrl, CreatedAt FROM Categories WHERE CID = @Id";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Id", id);

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            if (await reader.ReadAsync(cancellationToken))
            {
                return new Categories(
                    id: Convert.ToInt32(reader["CID"]),
                    name: reader["CName"]?.ToString() ?? "",
                    description: reader["CDescription"]?.ToString() ?? "",
                    imageUrl: reader["ImageUrl"]?.ToString() ?? "",
                    createdAt: reader["CreatedAt"] != DBNull.Value ? Convert.ToDateTime(reader["CreatedAt"]) : DateTime.UtcNow
                );
            }

            return null;
        }

        public async Task<Categories> AddCategoryAsync(Categories category, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "INSERT INTO Categories (CName, CDescription, ImageUrl, CreatedAt) OUTPUT INSERTED.CID VALUES (@Name, @Description, @ImageUrl, @CreatedAt)";
            using var command = new SqlCommand(sql, connection);
            
            command.Parameters.AddWithValue("@Name", category.Name);
            command.Parameters.AddWithValue("@Description", category.Description);
            command.Parameters.AddWithValue("@ImageUrl", category.ImageUrl);
            command.Parameters.AddWithValue("@CreatedAt", category.CreatedAt);

            var newId = (int)await command.ExecuteScalarAsync(cancellationToken);
            return new Categories(newId, category.Name, category.Description, category.ImageUrl, category.CreatedAt);
        }

        public async Task<Categories> UpdateCategoryAsync(Categories category, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "UPDATE Categories SET CName = @Name, CDescription = @Description, ImageUrl = @ImageUrl WHERE CID = @Id";
            using var command = new SqlCommand(sql, connection);
            
            command.Parameters.AddWithValue("@Id", category.Id);
            command.Parameters.AddWithValue("@Name", category.Name);
            command.Parameters.AddWithValue("@Description", category.Description);
            command.Parameters.AddWithValue("@ImageUrl", category.ImageUrl);

            await command.ExecuteNonQueryAsync(cancellationToken);
            return category;
        }

        public async Task<bool> DeleteCategoryAsync(int id, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "DELETE FROM Categories WHERE CID = @Id";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Id", id);

            var rowsAffected = await command.ExecuteNonQueryAsync(cancellationToken);
            return rowsAffected > 0;
        }

        public async Task<int> GetCategoriesCountAsync(CategoryFilter filter, CancellationToken cancellationToken = default)
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

        public async Task<List<Categories>> GetActiveCategoriesAsync(CancellationToken cancellationToken = default)
        {
            var filter = new CategoryFilter { IsActive = true };
            return await GetCategoriesAsync(filter, cancellationToken);
        }

        private (string Sql, Dictionary<string, object> Parameters) BuildGetCategoriesQuery(CategoryFilter filter)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            var sql = "SELECT CID, CName, CDescription, ImageUrl, CreatedAt FROM Categories";

            if (!string.IsNullOrWhiteSpace(filter.SearchTerm))
            {
                conditions.Add("(CName LIKE @SearchTerm OR CDescription LIKE @SearchTerm)");
                parameters["@SearchTerm"] = $"%{filter.SearchTerm}%";
            }

            if (filter.IsActive.HasValue)
            {
                // 🎯 Adjust this based on your actual schema - you might have an IsActive column
                // conditions.Add("IsActive = @IsActive");
                // parameters["@IsActive"] = filter.IsActive.Value;
            }

            if (filter.CreatedAfter.HasValue)
            {
                conditions.Add("CreatedAt >= @CreatedAfter");
                parameters["@CreatedAfter"] = filter.CreatedAfter.Value;
            }

            if (filter.CreatedBefore.HasValue)
            {
                conditions.Add("CreatedAt <= @CreatedBefore");
                parameters["@CreatedBefore"] = filter.CreatedBefore.Value;
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            sql += " ORDER BY CName";

            var offset = (filter.PageNumber - 1) * filter.PageSize;
            sql += $" OFFSET {offset} ROWS FETCH NEXT {filter.PageSize} ROWS ONLY";

            return (sql, parameters);
        }

        private (string Sql, Dictionary<string, object> Parameters) BuildCountQuery(CategoryFilter filter)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            var sql = "SELECT COUNT(*) FROM Categories";

            if (!string.IsNullOrWhiteSpace(filter.SearchTerm))
            {
                conditions.Add("(CName LIKE @SearchTerm OR CDescription LIKE @SearchTerm)");
                parameters["@SearchTerm"] = $"%{filter.SearchTerm}%";
            }

            if (filter.IsActive.HasValue)
            {
                // 🎯 Adjust based on your schema
                // conditions.Add("IsActive = @IsActive");
                // parameters["@IsActive"] = filter.IsActive.Value;
            }

            if (filter.CreatedAfter.HasValue)
            {
                conditions.Add("CreatedAt >= @CreatedAfter");
                parameters["@CreatedAfter"] = filter.CreatedAfter.Value;
            }

            if (filter.CreatedBefore.HasValue)
            {
                conditions.Add("CreatedAt <= @CreatedBefore");
                parameters["@CreatedBefore"] = filter.CreatedBefore.Value;
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            return (sql, parameters);
        }
    }
}