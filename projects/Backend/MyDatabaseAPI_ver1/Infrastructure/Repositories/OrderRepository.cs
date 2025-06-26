using Microsoft.Data.SqlClient;
using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Infrastructure.Repositories
{
    public class OrderRepository : IOrderRepository
    {
        private readonly string _connectionString;

        public OrderRepository(IConfiguration configuration)
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

        public async Task<List<Order>> GetOrdersAsync(OrderFilter filter, CancellationToken cancellationToken = default)
        {
            var orders = new List<Order>();
            var query = BuildGetOrdersQuery(filter);

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
                var order = new Order(
                    id: Convert.ToInt32(reader["OID"]),
                    userId: Convert.ToInt32(reader["UID"]),
                    orderStatus: reader["OrderStatus"]?.ToString() ?? "Pending",
                    totalAmount: Convert.ToDecimal(reader["TotalAmount"]),
                    deliveryAddress: reader["DeliveryAddress"]?.ToString() ?? "",
                    orderDate: reader["OrderDate"] != DBNull.Value ? Convert.ToDateTime(reader["OrderDate"]) : DateTime.UtcNow,
                    paymentMethod: reader["PaymentMethod"]?.ToString() ?? ""
                );
                orders.Add(order);
            }

            return orders;
        }

        public async Task<Order?> GetOrderByIdAsync(int id, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "SELECT OID, UID, OrderStatus, TotalAmount, DeliveryAddress, OrderDate, PaymentMethod FROM Orders WHERE OID = @Id";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Id", id);

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            if (await reader.ReadAsync(cancellationToken))
            {
                return new Order(
                    id: Convert.ToInt32(reader["OID"]),
                    userId: Convert.ToInt32(reader["UID"]),
                    orderStatus: reader["OrderStatus"]?.ToString() ?? "Pending",
                    totalAmount: Convert.ToDecimal(reader["TotalAmount"]),
                    deliveryAddress: reader["DeliveryAddress"]?.ToString() ?? "",
                    orderDate: reader["OrderDate"] != DBNull.Value ? Convert.ToDateTime(reader["OrderDate"]) : DateTime.UtcNow,
                    paymentMethod: reader["PaymentMethod"]?.ToString() ?? ""
                );
            }

            return null;
        }

        // ✅ FIXED: This method was using OiID instead of OrderItemID
        public async Task<Order?> GetOrderWithItemsAsync(int id, CancellationToken cancellationToken = default)
        {
            var order = await GetOrderByIdAsync(id, cancellationToken);
            if (order == null) return null;

            // Load order items separately
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            // ✅ FIXED: Changed from "OiID" to "OrderItemID"
            var sql = @"SELECT OrderItemID, OID, PID, Quantity, UnitPrice, TotalPrice 
                       FROM OrderItems 
                       WHERE OID = @OrderId";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@OrderId", id);

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
                order.AddOrderItem(orderItem);
            }

            return order;
        }

        public async Task<List<Order>> GetOrdersByUserIdAsync(int userId, CancellationToken cancellationToken = default)
        {
            var filter = new OrderFilter { UserId = userId };
            return await GetOrdersAsync(filter, cancellationToken);
        }

        // ✅ FIXED: ANSI-compliant AddOrderAsync
        public async Task<Order> AddOrderAsync(Order order, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            // ⭐ CHANGE: Use CURRENT_TIMESTAMP and separate INSERT/SELECT
            var sql = @"INSERT INTO Orders (UID, OrderStatus, TotalAmount, DeliveryAddress, OrderDate, PaymentMethod) 
                       VALUES (@UserId, @OrderStatus, @TotalAmount, @DeliveryAddress, CURRENT_TIMESTAMP, @PaymentMethod);
                       SELECT OID, UID, OrderStatus, TotalAmount, DeliveryAddress, OrderDate, PaymentMethod 
                       FROM Orders WHERE OID = SCOPE_IDENTITY();";
            
            using var command = new SqlCommand(sql, connection);
            
            command.Parameters.AddWithValue("@UserId", order.UserId);
            command.Parameters.AddWithValue("@OrderStatus", order.OrderStatus);
            command.Parameters.AddWithValue("@TotalAmount", order.TotalAmount);
            command.Parameters.AddWithValue("@DeliveryAddress", order.DeliveryAddress);
            command.Parameters.AddWithValue("@PaymentMethod", order.PaymentMethod);
            // ⭐ REMOVED: @OrderDate parameter - now using CURRENT_TIMESTAMP

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            if (await reader.ReadAsync(cancellationToken))
            {
                return new Order(
                    id: Convert.ToInt32(reader["OID"]),
                    userId: Convert.ToInt32(reader["UID"]),
                    orderStatus: reader["OrderStatus"]?.ToString() ?? "",
                    totalAmount: Convert.ToDecimal(reader["TotalAmount"]),
                    deliveryAddress: reader["DeliveryAddress"]?.ToString() ?? "",
                    orderDate: reader["OrderDate"] != DBNull.Value ? Convert.ToDateTime(reader["OrderDate"]) : DateTime.UtcNow,
                    paymentMethod: reader["PaymentMethod"]?.ToString() ?? ""
                );
            }
            
            throw new InvalidOperationException("Failed to insert order");
        }

        public async Task<Order> UpdateOrderAsync(Order order, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = @"UPDATE Orders 
                       SET OrderStatus = @OrderStatus, TotalAmount = @TotalAmount, 
                           DeliveryAddress = @DeliveryAddress, PaymentMethod = @PaymentMethod 
                       WHERE OID = @Id";
            using var command = new SqlCommand(sql, connection);
            
            command.Parameters.AddWithValue("@Id", order.Id);
            command.Parameters.AddWithValue("@OrderStatus", order.OrderStatus);
            command.Parameters.AddWithValue("@TotalAmount", order.TotalAmount);
            command.Parameters.AddWithValue("@DeliveryAddress", order.DeliveryAddress);
            command.Parameters.AddWithValue("@PaymentMethod", order.PaymentMethod);

            await command.ExecuteNonQueryAsync(cancellationToken);
            return order;
        }

        public async Task<bool> DeleteOrderAsync(int id, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "DELETE FROM Orders WHERE OID = @Id";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Id", id);

            var rowsAffected = await command.ExecuteNonQueryAsync(cancellationToken);
            return rowsAffected > 0;
        }

        public async Task<int> GetOrdersCountAsync(OrderFilter filter, CancellationToken cancellationToken = default)
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

        public async Task<List<Order>> GetOrdersByStatusAsync(string status, CancellationToken cancellationToken = default)
        {
            var filter = new OrderFilter { OrderStatus = status };
            return await GetOrdersAsync(filter, cancellationToken);
        }

        public async Task<List<Order>> GetOrdersByDateRangeAsync(DateTime startDate, DateTime endDate, CancellationToken cancellationToken = default)
        {
            var filter = new OrderFilter { OrderDateFrom = startDate, OrderDateTo = endDate };
            return await GetOrdersAsync(filter, cancellationToken);
        }

        private (string Sql, Dictionary<string, object> Parameters) BuildGetOrdersQuery(OrderFilter filter)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            var sql = "SELECT OID, UID, OrderStatus, TotalAmount, DeliveryAddress, OrderDate, PaymentMethod FROM Orders";

            if (filter.UserId.HasValue)
            {
                conditions.Add("UID = @UserId");
                parameters["@UserId"] = filter.UserId.Value;
            }

            if (!string.IsNullOrWhiteSpace(filter.OrderStatus))
            {
                conditions.Add("OrderStatus = @OrderStatus");
                parameters["@OrderStatus"] = filter.OrderStatus;
            }

            if (filter.OrderDateFrom.HasValue)
            {
                conditions.Add("OrderDate >= @OrderDateFrom");
                parameters["@OrderDateFrom"] = filter.OrderDateFrom.Value;
            }

            if (filter.OrderDateTo.HasValue)
            {
                conditions.Add("OrderDate <= @OrderDateTo");
                parameters["@OrderDateTo"] = filter.OrderDateTo.Value;
            }

            if (filter.MinAmount.HasValue)
            {
                conditions.Add("TotalAmount >= @MinAmount");
                parameters["@MinAmount"] = filter.MinAmount.Value;
            }

            if (filter.MaxAmount.HasValue)
            {
                conditions.Add("TotalAmount <= @MaxAmount");
                parameters["@MaxAmount"] = filter.MaxAmount.Value;
            }

            if (!string.IsNullOrWhiteSpace(filter.PaymentMethod))
            {
                conditions.Add("PaymentMethod = @PaymentMethod");
                parameters["@PaymentMethod"] = filter.PaymentMethod;
            }

            if (!string.IsNullOrWhiteSpace(filter.DeliveryAddress))
            {
                conditions.Add("DeliveryAddress LIKE @DeliveryAddress");
                parameters["@DeliveryAddress"] = $"%{filter.DeliveryAddress}%";
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            sql += " ORDER BY OrderDate DESC";

            var offset = (filter.PageNumber - 1) * filter.PageSize;
            sql += $" OFFSET {offset} ROWS FETCH NEXT {filter.PageSize} ROWS ONLY";

            return (sql, parameters);
        }

        private (string Sql, Dictionary<string, object> Parameters) BuildCountQuery(OrderFilter filter)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            var sql = "SELECT COUNT(*) FROM Orders";

            if (filter.UserId.HasValue)
            {
                conditions.Add("UID = @UserId");
                parameters["@UserId"] = filter.UserId.Value;
            }

            if (!string.IsNullOrWhiteSpace(filter.OrderStatus))
            {
                conditions.Add("OrderStatus = @OrderStatus");
                parameters["@OrderStatus"] = filter.OrderStatus;
            }

            if (filter.OrderDateFrom.HasValue)
            {
                conditions.Add("OrderDate >= @OrderDateFrom");
                parameters["@OrderDateFrom"] = filter.OrderDateFrom.Value;
            }

            if (filter.OrderDateTo.HasValue)
            {
                conditions.Add("OrderDate <= @OrderDateTo");
                parameters["@OrderDateTo"] = filter.OrderDateTo.Value;
            }

            if (filter.MinAmount.HasValue)
            {
                conditions.Add("TotalAmount >= @MinAmount");
                parameters["@MinAmount"] = filter.MinAmount.Value;
            }

            if (filter.MaxAmount.HasValue)
            {
                conditions.Add("TotalAmount <= @MaxAmount");
                parameters["@MaxAmount"] = filter.MaxAmount.Value;
            }

            if (!string.IsNullOrWhiteSpace(filter.PaymentMethod))
            {
                conditions.Add("PaymentMethod = @PaymentMethod");
                parameters["@PaymentMethod"] = filter.PaymentMethod;
            }

            if (!string.IsNullOrWhiteSpace(filter.DeliveryAddress))
            {
                conditions.Add("DeliveryAddress LIKE @DeliveryAddress");
                parameters["@DeliveryAddress"] = $"%{filter.DeliveryAddress}%";
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            return (sql, parameters);
        }
    }
}