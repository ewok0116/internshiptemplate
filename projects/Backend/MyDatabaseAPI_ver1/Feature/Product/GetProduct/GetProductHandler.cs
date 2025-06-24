
// Features/Products/GetProducts/GetProductsHandler.cs
using MediatR;
using Microsoft.Data.SqlClient;
using MyFoodOrderingAPI.Core.Entities;

namespace MyFoodOrderingAPI.Features.Products.GetProducts
{
    public class GetProductsHandler : IRequestHandler<GetProductsQuery, GetProductsResponse>
    {
        private readonly string _connectionString;

        public GetProductsHandler(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection") ??
                throw new InvalidOperationException("Connection string not found.");
        }

        public async Task<GetProductsResponse> Handle(GetProductsQuery request, CancellationToken cancellationToken)
        {
            try
            {
                // 1. Database'den Entity'leri çek
                var productEntities = await GetProductsFromDatabaseAsync(request, cancellationToken);

                // 2. Entity'lerden DTO'ya dönüştür
                var productDtos = productEntities.Select(entity => new ProductDto
                {
                    Id = entity.Id,
                    Name = entity.Name,
                    Description = entity.Description,
                    Price = entity.Price,
                    ImageUrl = entity.ImageUrl,
                    CategoryId = entity.CategoryId,
                    IsAvailable = entity.IsAvailable,
                    CreatedAt = entity.CreatedAt
                }).ToList();

                return new GetProductsResponse
                {
                    Products = productDtos,
                    TotalCount = productDtos.Count,
                    Success = true,
                    Message = "Products retrieved successfully"
                };
            }
            catch (Exception ex)
            {
                return new GetProductsResponse
                {
                    Success = false,
                    Message = ex.Message
                };
            }
        }

        private async Task<List<Product>> GetProductsFromDatabaseAsync(GetProductsQuery request, CancellationToken cancellationToken)
        {
            var products = new List<Product>();
            var query = BuildSqlQuery(request);

            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            using var command = new SqlCommand(query.Sql, connection);
            
            // Add parameters
            foreach (var param in query.Parameters)
            {
                command.Parameters.AddWithValue(param.Key, param.Value);
            }

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            while (await reader.ReadAsync(cancellationToken))
            {
                // ✅ FIXED: Using your actual column names
                var product = new Product(
                    id: Convert.ToInt32(reader["PID"]),
                    name: reader["PName"]?.ToString() ?? "",
                    description: reader["PDescription"]?.ToString() ?? "",
                    price: Convert.ToDecimal(reader["Price"]),
                    imageUrl: reader["PImageUrl"]?.ToString() ?? "",
                    categoryId: reader["CID"] != DBNull.Value ? Convert.ToInt32(reader["CID"]) : 0,
                    isAvailable: reader["IsAvailable"] != DBNull.Value ? Convert.ToBoolean(reader["IsAvailable"]) : true,
                    createdAt: reader["CreatedAt"] != DBNull.Value ? Convert.ToDateTime(reader["CreatedAt"]) : DateTime.UtcNow
                );
                products.Add(product);
            }

            return products;
        }

        private (string Sql, Dictionary<string, object> Parameters) BuildSqlQuery(GetProductsQuery request)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            // ✅ FIXED: Using your actual column names
            var sql = "SELECT PID, PName, PDescription, Price, PImageUrl, CID, IsAvailable, CreatedAt FROM Products";

            if (!string.IsNullOrWhiteSpace(request.SearchTerm))
            {
                conditions.Add("(PName LIKE @SearchTerm OR PDescription LIKE @SearchTerm)");
                parameters["@SearchTerm"] = $"%{request.SearchTerm}%";
            }

            if (request.CategoryId.HasValue)
            {
                conditions.Add("CID = @CategoryId");
                parameters["@CategoryId"] = request.CategoryId.Value;
            }

            if (request.MinPrice.HasValue)
            {
                conditions.Add("Price >= @MinPrice");
                parameters["@MinPrice"] = request.MinPrice.Value;
            }

            if (request.MaxPrice.HasValue)
            {
                conditions.Add("Price <= @MaxPrice");
                parameters["@MaxPrice"] = request.MaxPrice.Value;
            }

            // ✅ FIXED: Using IsAvailable instead of IsActive
            if (request.IsAvailable.HasValue)
            {
                conditions.Add("IsAvailable = @IsAvailable");
                parameters["@IsAvailable"] = request.IsAvailable.Value;
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            sql += " ORDER BY PName";

            return (sql, parameters);
        }
    }
}
