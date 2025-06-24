// Features/OrderItems/GetOrderItems/GetOrderItemHandler.cs
using MediatR;
using Microsoft.Data.SqlClient;

namespace MyFoodOrderingAPI.Features.OrderItems.GetOrderItems
{
    public class GetOrderItemHandler : IRequestHandler<GetOrderItemQuery, GetOrderItemResponse>
    {
        private readonly string _connectionString;

        public GetOrderItemHandler(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection") ??
                throw new InvalidOperationException("Connection string not found.");
        }

        public async Task<GetOrderItemResponse> Handle(GetOrderItemQuery request, CancellationToken cancellationToken)
        {
            try
            {
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync(cancellationToken);

                // ✅ GET SPECIFIC ORDER ITEM WITH COMPLETE PRODUCT DETAILS
                using var command = new SqlCommand(@"
                    SELECT 
                        oi.OIID, oi.OID, oi.PID, oi.Quantity, oi.UnitPrice, oi.TotalPrice,
                        p.PName, p.PDescription, p.ImageUrl, p.PPrice as CurrentPrice
                    FROM OrderItems oi
                    JOIN Products p ON oi.PID = p.PID
                    WHERE oi.OID = @OrderId AND oi.OIID = @ItemId", connection);

                command.Parameters.AddWithValue("@OrderId", request.OrderId);
                command.Parameters.AddWithValue("@ItemId", request.ItemId);

                using var reader = await command.ExecuteReaderAsync(cancellationToken);
                if (await reader.ReadAsync(cancellationToken))
                {
                    var orderItem = new OrderItemDetailDto
                    {
                        Id = Convert.ToInt32(reader["OIID"]),
                        OrderId = Convert.ToInt32(reader["OID"]),
                        ProductId = Convert.ToInt32(reader["PID"]),
                        ProductName = reader["PName"]?.ToString() ?? "",
                        ProductDescription = reader["PDescription"]?.ToString() ?? "",
                        ProductImageUrl = reader["ImageUrl"]?.ToString() ?? "",
                        Quantity = Convert.ToInt32(reader["Quantity"]),
                        UnitPrice = Convert.ToDecimal(reader["UnitPrice"]),
                        TotalPrice = Convert.ToDecimal(reader["TotalPrice"])
                    };

                    // ✅ VALIDATE CALCULATION
                    var calculatedTotal = Math.Round(orderItem.Quantity * orderItem.UnitPrice, 2);
                    orderItem.IsValid = Math.Abs(orderItem.TotalPrice - calculatedTotal) < 0.01m;

                    // ✅ CHECK IF PRICE HAS CHANGED SINCE ORDER
                    var currentPrice = Convert.ToDecimal(reader["CurrentPrice"]);
                    var priceChanged = Math.Abs(orderItem.UnitPrice - currentPrice) > 0.01m;

                    return new GetOrderItemResponse
                    {
                        OrderItem = orderItem,
                        Success = true,
                        Message = priceChanged 
                            ? $"Order item found. Note: Product price has changed from ${orderItem.UnitPrice} to ${currentPrice}"
                            : "Order item found"
                    };
                }
                else
                {
                    return new GetOrderItemResponse
                    {
                        Success = false,
                        Message = $"Order item {request.ItemId} not found in order {request.OrderId}"
                    };
                }
            }
            catch (Exception ex)
            {
                return new GetOrderItemResponse
                {
                    Success = false,
                    Message = ex.Message
                };
            }
        }
    }
}