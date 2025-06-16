using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using System.Data;

namespace MyDatabaseAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DatabaseController : ControllerBase
    {
        private readonly string _connectionString = "Server=192.168.1.101,1433;Database=FoodOrdering;User Id=ecoli;Password=101522;TrustServerCertificate=true;";

        [HttpGet("test-connection")]
        public async Task<IActionResult> TestConnection()
        {
            try
            {
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();
                return Ok(new { message = "Database connection successful!", timestamp = DateTime.Now });
            }
            catch (Exception ex)
            {
                return BadRequest(new { error = ex.Message });
            }
        }

        [HttpGet("users")]
        public async Task<IActionResult> GetUsers()
        {
            try
            {
                var users = new List<object>();
                
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqlCommand("SELECT UID, UName, UEmail FROM Users", connection);
                using var reader = await command.ExecuteReaderAsync();
                
                while (await reader.ReadAsync())
                {
                    users.Add(new
                    {
                        id = reader["UID"],
                        name = reader["UName"],
                        email = reader["UEmail"]
                    });
                }
                
                return Ok(users);
            }
            catch (Exception ex)
            {
                return BadRequest(new { error = ex.Message });
            }
        }

        [HttpGet("products")]
        public async Task<IActionResult> GetProducts()
        {
            try
            {
                var products = new List<object>();
                
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqlCommand("SELECT PID, PName, PDescription, Price FROM Products", connection);
                using var reader = await command.ExecuteReaderAsync();
                
                while (await reader.ReadAsync())
                {
                    products.Add(new
                    {
                        id = reader["PID"],
                        name = reader["PName"],
                        description = reader["PDescription"],
                        price = reader["Price"]
                    });
                }
                
                return Ok(products);
            }
            catch (Exception ex)
            {
                return BadRequest(new { error = ex.Message });
            }
        }

        [HttpGet("categories")]
        public async Task<IActionResult> GetCategories()
        {
            try
            {
                var categories = new List<object>();
                
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqlCommand("SELECT CID, CName, CDescription FROM Categories", connection);
                using var reader = await command.ExecuteReaderAsync();
                
                while (await reader.ReadAsync())
                {
                    categories.Add(new
                    {
                        id = reader["CID"],
                        name = reader["CName"],
                        description = reader["CDescription"]
                    });
                }
                
                return Ok(categories);
            }
            catch (Exception ex)
            {
                return BadRequest(new { error = ex.Message });
            }
        }

        [HttpGet("orders")]
        public async Task<IActionResult> GetOrders()
        {
            try
            {
                var orders = new List<object>();
                
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqlCommand("SELECT OID, UID, OrderStatus, TotalAmount, OrderDate, DeliveryAddress, PaymentMethod FROM Orders", connection);
                using var reader = await command.ExecuteReaderAsync();
                
                while (await reader.ReadAsync())
                {
                    orders.Add(new
                    {
                        id = reader["OID"],
                        userId = reader["UID"],
                        status = reader["OrderStatus"],
                        totalAmount = reader["TotalAmount"],
                        orderDate = reader["OrderDate"],
                        deliveryAddress = reader["DeliveryAddress"],
                        paymentMethod = reader["PaymentMethod"]
                    });
                }
                
                return Ok(orders);
            }
            catch (Exception ex)
            {
                return BadRequest(new { error = ex.Message });
            }
        }

        // NEW: Create Order Endpoint
        [HttpPost("orders")]
        public async Task<IActionResult> CreateOrder([FromBody] CreateOrderRequest request)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync();
            
            using var transaction = connection.BeginTransaction();
            
            try
            {
                // Insert the main order
                using var orderCommand = new SqlCommand(@"
                    INSERT INTO Orders (UID, TotalAmount, OrderDate, OrderStatus, DeliveryAddress, PaymentMethod) 
                    VALUES (@UserId, @TotalAmount, @OrderDate, @OrderStatus, @DeliveryAddress, @PaymentMethod); 
                    SELECT SCOPE_IDENTITY();", connection, transaction);
                
                orderCommand.Parameters.AddWithValue("@UserId", request.UserId);
                orderCommand.Parameters.AddWithValue("@TotalAmount", request.TotalAmount);
                orderCommand.Parameters.AddWithValue("@OrderDate", DateTime.Now);
                orderCommand.Parameters.AddWithValue("@OrderStatus", "Pending");
                orderCommand.Parameters.AddWithValue("@DeliveryAddress", request.DeliveryAddress);
                orderCommand.Parameters.AddWithValue("@PaymentMethod", request.PaymentMethod);
                
                var orderId = Convert.ToInt32(await orderCommand.ExecuteScalarAsync());
                
                // Insert order items
                foreach (var item in request.OrderItems)
                {
                    using var itemCommand = new SqlCommand(@"
                        INSERT INTO OrderItems (OID, PID, Quantity, UnitPrice, TotalPrice) 
                        VALUES (@OrderId, @ProductId, @Quantity, @UnitPrice, @TotalPrice)", connection, transaction);
                    
                    itemCommand.Parameters.AddWithValue("@OrderId", orderId);
                    itemCommand.Parameters.AddWithValue("@ProductId", item.ProductId);
                    itemCommand.Parameters.AddWithValue("@Quantity", item.Quantity);
                    itemCommand.Parameters.AddWithValue("@UnitPrice", item.UnitPrice);
                    itemCommand.Parameters.AddWithValue("@TotalPrice", item.TotalPrice);
                    
                    await itemCommand.ExecuteNonQueryAsync();
                }
                
                transaction.Commit();
                
                return Ok(new CreateOrderResponse
                {
                    OrderId = orderId,
                    Message = "Order created successfully",
                    OrderDate = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss")
                });
            }
            catch (Exception ex)
            {
                transaction.Rollback();
                return BadRequest(new { error = ex.Message });
            }
        }

        // NEW: Get Order Items for a specific order
        [HttpGet("orders/{orderId}/items")]
        public async Task<IActionResult> GetOrderItems(int orderId)
        {
            try
            {
                var orderItems = new List<object>();
                
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqlCommand(@"
                    SELECT oi.OIID, oi.OID, oi.PID, oi.Quantity, oi.UnitPrice, oi.TotalPrice, p.PName
                    FROM OrderItems oi
                    INNER JOIN Products p ON oi.PID = p.PID
                    WHERE oi.OID = @OrderId", connection);
                
                command.Parameters.AddWithValue("@OrderId", orderId);
                using var reader = await command.ExecuteReaderAsync();
                
                while (await reader.ReadAsync())
                {
                    orderItems.Add(new
                    {
                        id = reader["OIID"],
                        orderId = reader["OID"],
                        productId = reader["PID"],
                        productName = reader["PName"],
                        quantity = reader["Quantity"],
                        unitPrice = reader["UnitPrice"],
                        totalPrice = reader["TotalPrice"]
                    });
                }
                
                return Ok(orderItems);
            }
            catch (Exception ex)
            {
                return BadRequest(new { error = ex.Message });
            }
        }

        // NEW: Update Order Status
        [HttpPut("orders/{orderId}/status")]
        public async Task<IActionResult> UpdateOrderStatus(int orderId, [FromBody] UpdateOrderStatusRequest request)
        {
            try
            {
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqlCommand(
                    "UPDATE Orders SET OrderStatus = @Status WHERE OID = @OrderId", connection);
                
                command.Parameters.AddWithValue("@Status", request.Status);
                command.Parameters.AddWithValue("@OrderId", orderId);
                
                var rowsAffected = await command.ExecuteNonQueryAsync();
                
                if (rowsAffected > 0)
                {
                    return Ok(new { message = "Order status updated successfully" });
                }
                else
                {
                    return NotFound(new { error = "Order not found" });
                }
            }
            catch (Exception ex)
            {
                return BadRequest(new { error = ex.Message });
            }
        }

        [HttpPost("users")]
        public async Task<IActionResult> CreateUser([FromBody] CreateUserRequest request)
        {
            try
            {
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqlCommand(
                    "INSERT INTO Users (UName, UEmail) VALUES (@Name, @Email); SELECT SCOPE_IDENTITY();", 
                    connection);
                
                command.Parameters.AddWithValue("@Name", request.Name);
                command.Parameters.AddWithValue("@Email", request.Email);
                
                var newId = await command.ExecuteScalarAsync();
                
                return Ok(new { id = newId, message = "User created successfully" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { error = ex.Message });
            }
        }
    }

    // Existing model
    public class CreateUserRequest
    {
        public string Name { get; set; } = "";
        public string Email { get; set; } = "";
    }

    // NEW: Order-related models
    public class CreateOrderRequest
    {
        public int UserId { get; set; }
        public double TotalAmount { get; set; }
        public string DeliveryAddress { get; set; } = "";
        public string PaymentMethod { get; set; } = "";
        public List<CreateOrderItemRequest> OrderItems { get; set; } = new List<CreateOrderItemRequest>();
    }

    public class CreateOrderItemRequest
    {
        public int ProductId { get; set; }
        public int Quantity { get; set; }
        public double UnitPrice { get; set; }
        public double TotalPrice { get; set; }
    }

    public class CreateOrderResponse
    {
        public int OrderId { get; set; }
        public string Message { get; set; } = "";
        public string OrderDate { get; set; } = "";
    }

    public class UpdateOrderStatusRequest
    {
        public string Status { get; set; } = "";
    }
}