using Microsoft.Data.SqlClient;
using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Infrastructure.Repositories
{
    public class OrderItemRepository : IOrderItemRepository
    {
        private readonly string _connectionString;

        public OrderItemRepository(IConfiguration configuration)
        {
            _connectionString = GetConnectionString(configuration);
            
            if (string.IsNullOrEmpty(_connectionString))
            {
                throw new InvalidOperationException("Connection string not found in appsettings.json or environment variables.");
            }
        }

        private string GetConnectionString(IConfiguration configuration)
        {
            var configConnectionString = configuration.GetConnectionString("DefaultConnection");
            if (!string.IsNullOrEmpty(configConnectionString))
            {
                return configConnectionString;
            }

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

        public async Task<List<OrderItem>> GetOrderItemsAsync(OrderItemFilter filter, CancellationToken cancellationToken = default)
        {
            var orderItems = new List<OrderItem>();
            var query = BuildGetOrderItemsQuery(filter);

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
                var orderItem = new OrderItem(
                    id: Convert.ToInt32(reader["OrderItemID"]),        // ✅ FIXED: Changed from "OiID" to "OrderItemID"
                    orderId: Convert.ToInt32(reader["OID"]),
                    productId: Convert.ToInt32(reader["PID"]),
                    quantity: Convert.ToInt32(reader["Quantity"]),
                    unitPrice: Convert.ToDecimal(reader["UnitPrice"]),
                    totalPrice: Convert.ToDecimal(reader["TotalPrice"])
                );
                orderItems.Add(orderItem);
            }

            return orderItems;
        }

        public async Task<OrderItem?> GetOrderItemByIdAsync(int id, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            // ✅ FIXED: Changed from "OiID" to "OrderItemID"
            var sql = "SELECT OrderItemID, OID, PID, Quantity, UnitPrice, TotalPrice FROM OrderItems WHERE OrderItemID = @Id";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Id", id);

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            if (await reader.ReadAsync(cancellationToken))
            {
                return new OrderItem(
                    id: Convert.ToInt32(reader["OrderItemID"]),        // ✅ FIXED: Changed from "OiID" to "OrderItemID"
                    orderId: Convert.ToInt32(reader["OID"]),
                    productId: Convert.ToInt32(reader["PID"]),
                    quantity: Convert.ToInt32(reader["Quantity"]),
                    unitPrice: Convert.ToDecimal(reader["UnitPrice"]),
                    totalPrice: Convert.ToDecimal(reader["TotalPrice"])
                );
            }

            return null;
        }

        public async Task<List<OrderItem>> GetOrderItemsByOrderIdAsync(int orderId, CancellationToken cancellationToken = default)
        {
            var filter = new OrderItemFilter { OrderId = orderId };
            return await GetOrderItemsAsync(filter, cancellationToken);
        }

        public async Task<List<OrderItem>> GetOrderItemsByProductIdAsync(int productId, CancellationToken cancellationToken = default)
        {
            var filter = new OrderItemFilter { ProductId = productId };
            return await GetOrderItemsAsync(filter, cancellationToken);
        }
// Update your OrderItemRepository.AddOrderItemAsync method

public async Task<OrderItem> AddOrderItemAsync(OrderItem orderItem, CancellationToken cancellationToken = default)
{
    using var connection = new SqlConnection(_connectionString);
    await connection.OpenAsync(cancellationToken);
    
    // ✅ FIXED: Remove TotalPrice from INSERT since it's a computed column
    var sql = @"INSERT INTO OrderItems (OID, PID, Quantity, UnitPrice) 
               OUTPUT INSERTED.OrderItemID, INSERTED.OID, INSERTED.PID, INSERTED.Quantity, INSERTED.UnitPrice, INSERTED.TotalPrice
               VALUES (@OrderId, @ProductId, @Quantity, @UnitPrice)";
    
    using var command = new SqlCommand(sql, connection);
    
    command.Parameters.AddWithValue("@OrderId", orderItem.OrderId);
    command.Parameters.AddWithValue("@ProductId", orderItem.ProductId);
    command.Parameters.AddWithValue("@Quantity", orderItem.Quantity);
    command.Parameters.AddWithValue("@UnitPrice", orderItem.UnitPrice);
    // ❌ REMOVED: @TotalPrice parameter since it's computed

    using var reader = await command.ExecuteReaderAsync(cancellationToken);
    if (await reader.ReadAsync(cancellationToken))
    {
        return new OrderItem(
            id: Convert.ToInt32(reader["OrderItemID"]),
            orderId: Convert.ToInt32(reader["OID"]),
            productId: Convert.ToInt32(reader["PID"]),
            quantity: Convert.ToInt32(reader["Quantity"]),
            unitPrice: Convert.ToDecimal(reader["UnitPrice"]),
            totalPrice: Convert.ToDecimal(reader["TotalPrice"]) // ✅ Read the computed value
        );
    }
    
    throw new InvalidOperationException("Failed to insert order item");
}        public async Task<OrderItem> UpdateOrderItemAsync(OrderItem orderItem, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            // ✅ FIXED: Changed from "OiID" to "OrderItemID"
            var sql = @"UPDATE OrderItems 
                       SET Quantity = @Quantity, UnitPrice = @UnitPrice, TotalPrice = @TotalPrice 
                       WHERE OrderItemID = @Id";
            using var command = new SqlCommand(sql, connection);
            
            command.Parameters.AddWithValue("@Id", orderItem.Id);
            command.Parameters.AddWithValue("@Quantity", orderItem.Quantity);
            command.Parameters.AddWithValue("@UnitPrice", orderItem.UnitPrice);
            command.Parameters.AddWithValue("@TotalPrice", orderItem.TotalPrice);

            await command.ExecuteNonQueryAsync(cancellationToken);
            return orderItem;
        }

        public async Task<bool> DeleteOrderItemAsync(int id, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            // ✅ FIXED: Changed from "OiID" to "OrderItemID"
            var sql = "DELETE FROM OrderItems WHERE OrderItemID = @Id";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Id", id);

            var rowsAffected = await command.ExecuteNonQueryAsync(cancellationToken);
            return rowsAffected > 0;
        }

        public async Task<bool> DeleteOrderItemsByOrderIdAsync(int orderId, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "DELETE FROM OrderItems WHERE OID = @OrderId";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@OrderId", orderId);

            var rowsAffected = await command.ExecuteNonQueryAsync(cancellationToken);
            return rowsAffected > 0;
        }

        public async Task<int> GetOrderItemsCountAsync(OrderItemFilter filter, CancellationToken cancellationToken = default)
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

        public async Task<decimal> GetOrderTotalAsync(int orderId, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "SELECT ISNULL(SUM(TotalPrice), 0) FROM OrderItems WHERE OID = @OrderId";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@OrderId", orderId);

            var result = await command.ExecuteScalarAsync(cancellationToken);
            return Convert.ToDecimal(result);
        }

        private (string Sql, Dictionary<string, object> Parameters) BuildGetOrderItemsQuery(OrderItemFilter filter)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            // ✅ FIXED: Changed from "OiID" to "OrderItemID"
            var sql = "SELECT OrderItemID, OID, PID, Quantity, UnitPrice, TotalPrice FROM OrderItems";

            if (filter.OrderId.HasValue)
            {
                conditions.Add("OID = @OrderId");
                parameters["@OrderId"] = filter.OrderId.Value;
            }

            if (filter.ProductId.HasValue)
            {
                conditions.Add("PID = @ProductId");
                parameters["@ProductId"] = filter.ProductId.Value;
            }

            if (filter.MinQuantity.HasValue)
            {
                conditions.Add("Quantity >= @MinQuantity");
                parameters["@MinQuantity"] = filter.MinQuantity.Value;
            }

            if (filter.MaxQuantity.HasValue)
            {
                conditions.Add("Quantity <= @MaxQuantity");
                parameters["@MaxQuantity"] = filter.MaxQuantity.Value;
            }

            if (filter.MinPrice.HasValue)
            {
                conditions.Add("UnitPrice >= @MinPrice");
                parameters["@MinPrice"] = filter.MinPrice.Value;
            }

            if (filter.MaxPrice.HasValue)
            {
                conditions.Add("UnitPrice <= @MaxPrice");
                parameters["@MaxPrice"] = filter.MaxPrice.Value;
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            // ✅ FIXED: Changed from "OiID" to "OrderItemID"
            sql += " ORDER BY OrderItemID";

            var offset = (filter.PageNumber - 1) * filter.PageSize;
            sql += $" OFFSET {offset} ROWS FETCH NEXT {filter.PageSize} ROWS ONLY";

            return (sql, parameters);
        }

        private (string Sql, Dictionary<string, object> Parameters) BuildCountQuery(OrderItemFilter filter)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            var sql = "SELECT COUNT(*) FROM OrderItems";

            if (filter.OrderId.HasValue)
            {
                conditions.Add("OID = @OrderId");
                parameters["@OrderId"] = filter.OrderId.Value;
            }

            if (filter.ProductId.HasValue)
            {
                conditions.Add("PID = @ProductId");
                parameters["@ProductId"] = filter.ProductId.Value;
            }

            if (filter.MinQuantity.HasValue)
            {
                conditions.Add("Quantity >= @MinQuantity");
                parameters["@MinQuantity"] = filter.MinQuantity.Value;
            }

            if (filter.MaxQuantity.HasValue)
            {
                conditions.Add("Quantity <= @MaxQuantity");
                parameters["@MaxQuantity"] = filter.MaxQuantity.Value;
            }

            if (filter.MinPrice.HasValue)
            {
                conditions.Add("UnitPrice >= @MinPrice");
                parameters["@MinPrice"] = filter.MinPrice.Value;
            }

            if (filter.MaxPrice.HasValue)
            {
                conditions.Add("UnitPrice <= @MaxPrice");
                parameters["@MaxPrice"] = filter.MaxPrice.Value;
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            return (sql, parameters);
        }
    }
}