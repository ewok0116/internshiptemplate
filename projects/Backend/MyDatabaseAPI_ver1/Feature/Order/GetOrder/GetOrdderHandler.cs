// Features/Orders/GetOrders/GetOrdersHandler.cs
using MediatR;
using Microsoft.Data.SqlClient;
using MyFoodOrderingAPI.Core.Entities;

namespace MyFoodOrderingAPI.Features.Orders.GetOrders
{
    public class GetOrdersHandler : IRequestHandler<GetOrdersQuery, GetOrdersResponse>
    {
        private readonly string _connectionString;

        public GetOrdersHandler(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection") ??
                throw new InvalidOperationException("Connection string not found.");
        }

        public async Task<GetOrdersResponse> Handle(GetOrdersQuery request, CancellationToken cancellationToken)
        {
            try
            {
                var orders = await GetOrdersFromDatabase(request, cancellationToken);

                var orderDtos = new List<OrderSummaryDto>();
                foreach (var order in orders)
                {
                    var itemCount = await GetOrderItemCount(order.Id, cancellationToken);
                    var userName = await GetUserName(order.UserId, cancellationToken);
                    
                    orderDtos.Add(new OrderSummaryDto
                    {
                        Id = order.Id,
                        UserId = order.UserId,
                        UserName = userName,
                        OrderStatus = order.OrderStatus,
                        TotalAmount = order.TotalAmount,
                        DeliveryAddress = order.DeliveryAddress,
                        OrderDate = order.OrderDate,
                        PaymentMethod = order.PaymentMethod,
                        ItemCount = itemCount
                    });
                }

                return new GetOrdersResponse
                {
                    Orders = orderDtos,
                    TotalCount = orderDtos.Count,
                    Success = true,
                    Message = "Orders retrieved successfully"
                };
            }
            catch (Exception ex)
            {
                return new GetOrdersResponse
                {
                    Success = false,
                    Message = ex.Message
                };
            }
        }

        private async Task<List<Order>> GetOrdersFromDatabase(GetOrdersQuery request, CancellationToken cancellationToken)
        {
            var orders = new List<Order>();
            var query = BuildSqlQuery(request);

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
                    orderStatus: reader["OrderStatus"]?.ToString() ?? "",
                    totalAmount: Convert.ToDecimal(reader["TotalAmount"]),
                    deliveryAddress: reader["DeliveryAddress"]?.ToString() ?? "",
                    orderDate: Convert.ToDateTime(reader["OrderDate"]),
                    paymentMethod: reader["PaymentMethod"]?.ToString() ?? ""
                );
                orders.Add(order);
            }

            return orders;
        }

        private async Task<int> GetOrderItemCount(int orderId, CancellationToken cancellationToken)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            using var command = new SqlCommand("SELECT SUM(Quantity) FROM OrderItems WHERE OID = @OrderId", connection);
            command.Parameters.AddWithValue("@OrderId", orderId);
            
            var result = await command.ExecuteScalarAsync(cancellationToken);
            return result == DBNull.Value ? 0 : Convert.ToInt32(result);
        }

        private async Task<string> GetUserName(int userId, CancellationToken cancellationToken)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            using var command = new SqlCommand("SELECT UName FROM Users WHERE UID = @UserId", connection);
            command.Parameters.AddWithValue("@UserId", userId);
            
            var result = await command.ExecuteScalarAsync(cancellationToken);
            return result?.ToString() ?? "";
        }

        private (string Sql, Dictionary<string, object> Parameters) BuildSqlQuery(GetOrdersQuery request)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            var sql = "SELECT OID, UID, OrderStatus, TotalAmount, DeliveryAddress, OrderDate, PaymentMethod FROM Orders";

            if (request.UserId.HasValue)
            {
                conditions.Add("UID = @UserId");
                parameters["@UserId"] = request.UserId.Value;
            }

            if (!string.IsNullOrWhiteSpace(request.Status))
            {
                conditions.Add("OrderStatus = @Status");
                parameters["@Status"] = request.Status;
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            sql += " ORDER BY OrderDate DESC";

            return (sql, parameters);
        }
    }
}