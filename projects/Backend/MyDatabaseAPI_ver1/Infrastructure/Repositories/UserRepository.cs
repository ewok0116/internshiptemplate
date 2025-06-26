using Microsoft.Data.SqlClient;
using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Infrastructure.Repositories
{
    public class UserRepository : IUserRepository
    {
        private readonly string _connectionString;

        public UserRepository(IConfiguration configuration)
        {
            // 🎯 Try multiple sources for connection string
            _connectionString = GetConnectionString(configuration);
            
            if (string.IsNullOrEmpty(_connectionString))
            {
                throw new InvalidOperationException("Connection string not found in appsettings.json or environment variables.");
            }
        }

        private string GetConnectionString(IConfiguration configuration)
        {
            // 🎯 Try appsettings.json first
            var configConnectionString = configuration.GetConnectionString("DefaultConnection");
            if (!string.IsNullOrEmpty(configConnectionString))
            {
                return configConnectionString;
            }

            // 🎯 Fallback to environment variables (same as DatabaseController)
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

        public async Task<List<User>> GetUsersAsync(UserFilter filter, CancellationToken cancellationToken = default)
        {
            var users = new List<User>();
            var query = BuildGetUsersQuery(filter);

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
                var user = new User(
                    id: Convert.ToInt32(reader["UID"]),
                    name: reader["UName"]?.ToString() ?? "",
                    email: reader["UEmail"]?.ToString() ?? ""
                );
                users.Add(user);
            }

            return users;
        }

        public async Task<User?> GetUserByIdAsync(int id, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "SELECT UID, UName, UEmail FROM Users WHERE UID = @Id";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Id", id);

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            if (await reader.ReadAsync(cancellationToken))
            {
                return new User(
                    id: Convert.ToInt32(reader["UID"]),
                    name: reader["UName"]?.ToString() ?? "",
                    email: reader["UEmail"]?.ToString() ?? ""
                );
            }

            return null;
        }

        public async Task<User?> GetUserByEmailAsync(string email, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "SELECT UID, UName, UEmail FROM Users WHERE UEmail = @Email";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Email", email);

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            if (await reader.ReadAsync(cancellationToken))
            {
                return new User(
                    id: Convert.ToInt32(reader["UID"]),
                    name: reader["UName"]?.ToString() ?? "",
                    email: reader["UEmail"]?.ToString() ?? ""
                );
            }

            return null;
        }

        // ✅ FIXED: ANSI-compliant AddUserAsync
        public async Task<User> AddUserAsync(User user, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            // ⭐ CHANGE: Use CURRENT_TIMESTAMP and separate INSERT/SELECT
            var sql = @"INSERT INTO Users (UName, UEmail, CreatedAt) 
                       VALUES (@Name, @Email, CURRENT_TIMESTAMP);
                       SELECT UID, UName, UEmail, CreatedAt 
                       FROM Users WHERE UID = SCOPE_IDENTITY();";
            
            using var command = new SqlCommand(sql, connection);
            
            command.Parameters.AddWithValue("@Name", user.Name);
            command.Parameters.AddWithValue("@Email", user.Email);
            // ⭐ REMOVED: @CreatedAt parameter - now using CURRENT_TIMESTAMP

            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            if (await reader.ReadAsync(cancellationToken))
            {
                return new User(
                    id: Convert.ToInt32(reader["UID"]),
                    name: reader["UName"]?.ToString() ?? "",
                    email: reader["UEmail"]?.ToString() ?? ""
                );
            }

            throw new InvalidOperationException("Failed to insert user");
        }

        public async Task<User> UpdateUserAsync(User user, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "UPDATE Users SET UName = @Name, UEmail = @Email WHERE UID = @Id";
            using var command = new SqlCommand(sql, connection);
            
            command.Parameters.AddWithValue("@Id", user.Id);
            command.Parameters.AddWithValue("@Name", user.Name);
            command.Parameters.AddWithValue("@Email", user.Email);

            await command.ExecuteNonQueryAsync(cancellationToken);
            return user;
        }

        public async Task<bool> DeleteUserAsync(int id, CancellationToken cancellationToken = default)
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            var sql = "DELETE FROM Users WHERE UID = @Id";
            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Id", id);

            var rowsAffected = await command.ExecuteNonQueryAsync(cancellationToken);
            return rowsAffected > 0;
        }

        public async Task<int> GetUsersCountAsync(UserFilter filter, CancellationToken cancellationToken = default)
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

        private (string Sql, Dictionary<string, object> Parameters) BuildGetUsersQuery(UserFilter filter)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            var sql = "SELECT UID, UName, UEmail FROM Users";

            if (!string.IsNullOrWhiteSpace(filter.SearchTerm))
            {
                conditions.Add("(UName LIKE @SearchTerm OR UEmail LIKE @SearchTerm)");
                parameters["@SearchTerm"] = $"%{filter.SearchTerm}%";
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            sql += " ORDER BY UName";

            var offset = (filter.PageNumber - 1) * filter.PageSize;
            sql += $" OFFSET {offset} ROWS FETCH NEXT {filter.PageSize} ROWS ONLY";

            return (sql, parameters);
        }

        private (string Sql, Dictionary<string, object> Parameters) BuildCountQuery(UserFilter filter)
        {
            var conditions = new List<string>();
            var parameters = new Dictionary<string, object>();

            var sql = "SELECT COUNT(*) FROM Users";

            if (!string.IsNullOrWhiteSpace(filter.SearchTerm))
            {
                conditions.Add("(UName LIKE @SearchTerm OR UEmail LIKE @SearchTerm)");
                parameters["@SearchTerm"] = $"%{filter.SearchTerm}%";
            }

            if (conditions.Any())
            {
                sql += " WHERE " + string.Join(" AND ", conditions);
            }

            return (sql, parameters);
        }
    }
}