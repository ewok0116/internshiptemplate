using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;

namespace MyDatabaseAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DatabaseController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public DatabaseController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        // Get connection string from environment variables
        private string GetConnectionString()
        {
            var server = Environment.GetEnvironmentVariable("DB_SERVER");
            var database = Environment.GetEnvironmentVariable("DB_NAME");
            var user = Environment.GetEnvironmentVariable("DB_USER");
            var password = Environment.GetEnvironmentVariable("DB_PASSWORD");
            
            return $"Server={server};Database={database};User Id={user};Password={password};TrustServerCertificate=true;Encrypt=false;";
        }

        [HttpGet("test-connection")]
        public async Task<IActionResult> TestConnection()
        {
            try
            {
                using var connection = new SqlConnection(GetConnectionString());
                await connection.OpenAsync();
                
                return Ok(new { 
                    message = "Database connection successful!", 
                    timestamp = DateTime.Now,
                    server = connection.DataSource,
                    database = connection.Database 
                });
            }
            catch (Exception ex)
            {
                return BadRequest(new { 
                    error = ex.Message,
                    innerError = ex.InnerException?.Message
                });
            }
        }

        [HttpGet("debug-env")]
        public IActionResult DebugEnv()
        {
            return Ok(new {
                DB_SERVER = Environment.GetEnvironmentVariable("DB_SERVER"),
                DB_NAME = Environment.GetEnvironmentVariable("DB_NAME"),
                DB_USER = Environment.GetEnvironmentVariable("DB_USER"),
                DB_PASSWORD = Environment.GetEnvironmentVariable("DB_PASSWORD") != null ? "***" : null,
                CurrentDirectory = Directory.GetCurrentDirectory(),
                EnvFileExists = System.IO.File.Exists(".env")
            });
        }

        // NOTE: CRUD operations are handled by Feature folder controllers:
        // - Users: Feature/User/Getuser/GetUserController.cs
        // - Products: Feature/Product/GetProduct/GetProductController.cs  
        // - Categories: Feature/Categories/GetCategory/GetCategoryController.cs
        // - Orders: Feature/Order/GetOrder/GetOrderController.cs + CreateOrder/CreateOrderController.cs
        // - OrderItems: Feature/OrderItem/GetOrderItem/GetOrderItemController.cs
    }
}