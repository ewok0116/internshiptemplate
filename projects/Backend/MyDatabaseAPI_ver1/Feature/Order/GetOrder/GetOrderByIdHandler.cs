// Features/Orders/GetOrders/GetOrderByIdHandler.cs
using MediatR;
using Microsoft.Data.SqlClient;

namespace MyFoodOrderingAPI.Features.Orders.GetOrders
{
    public class GetOrderByIdHandler : IRequestHandler<GetOrderByIdQuery, GetOrderByIdResponse>
    {
        private readonly string _connectionString;

        public GetOrderByIdHandler(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection") ??
                throw new InvalidOperationException("Connection string not found.");
        }

        public async Task<GetOrderByIdResponse> Handle(GetOrderByIdQuery request, CancellationToken cancellationToken)
        {
            try
            {
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync(cancellationToken);

                // ✅ GET ORDER WITH USER NAME
                using var orderCommand = new SqlCommand(@"
                    SELECT o.OID, o.UID, o.OrderStatus, o.TotalAmount, o.DeliveryAddress, o.OrderDate, o.PaymentMethod, u.UName
                    FROM Orders o
                    JOIN Users u ON o.UID = u.UID
                    WHERE o.OID = @OrderId", connection);
                orderCommand.Parameters.AddWithValue("@OrderId", request.OrderId);

                using var orderReader = await orderCommand.ExecuteReaderAsync(cancellationToken);
                if (!await orderReader.ReadAsync(cancellationToken))
                {
                    return new GetOrderByIdResponse
                    {
                        Success = false,
                        Message = "Order not found"
                    };
                }

                var orderDetail = new OrderDetailDto
                {
                    Id = Convert.ToInt32(orderReader["OID"]),
                    UserId = Convert.ToInt32(orderReader["UID"]),
                    UserName = orderReader["UName"]?.ToString() ?? "",
                    OrderStatus = orderReader["OrderStatus"]?.ToString() ?? "",
                    TotalAmount = Convert.ToDecimal(orderReader["TotalAmount"]),
                    DeliveryAddress = orderReader["DeliveryAddress"]?.ToString() ?? "",
                    OrderDate = Convert.ToDateTime(orderReader["OrderDate"]),
                    PaymentMethod = orderReader["PaymentMethod"]?.ToString() ?? ""
                };

                // ✅ GET ORDER ITEMS WITH PRODUCT NAMES
                orderReader.Close();
                using var itemCommand = new SqlCommand(@"
                    SELECT oi.OIID, oi.PID, oi.Quantity, oi.UnitPrice, oi.TotalPrice, p.PName
                    FROM OrderItems oi
                    JOIN Products p ON oi.PID = p.PID
                    WHERE oi.OID = @OrderId
                    ORDER BY oi.OIID", connection);
                itemCommand.Parameters.AddWithValue("@OrderId", request.OrderId);

                using var itemReader = await itemCommand.ExecuteReaderAsync(cancellationToken);
                while (await itemReader.ReadAsync(cancellationToken))
                {
                    orderDetail.OrderItems.Add(new OrderItemDetailDto
                    {
                        Id = Convert.ToInt32(itemReader["OIID"]),
                        ProductId = Convert.ToInt32(itemReader["PID"]),
                        ProductName = itemReader["PName"]?.ToString() ?? "",
                        Quantity = Convert.ToInt32(itemReader["Quantity"]),
                        UnitPrice = Convert.ToDecimal(itemReader["UnitPrice"]),
                        TotalPrice = Convert.ToDecimal(itemReader["TotalPrice"])
                    });
                }

                return new GetOrderByIdResponse
                {
                    Order = orderDetail,
                    Success = true,
                    Message = "Order found successfully"
                };
            }
            catch (Exception ex)
            {
                return new GetOrderByIdResponse
                {
                    Success = false,
                    Message = ex.Message
                };
            }
        }
    }
}