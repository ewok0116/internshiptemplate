using MediatR;
using Microsoft.Data.SqlClient;
using MyFoodOrderingAPI.Core.Entities;

namespace MyFoodOrderingAPI.Features.Users.GetUsers
{
    public class GetUsersHandler : IRequestHandler<GetUsersQuery, GetUsersResponse>
    {
        private readonly string _connectionString;

        public GetUsersHandler(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection") ??
                throw new InvalidOperationException("Connection string not found.");
        }

        public async Task<GetUsersResponse> Handle(GetUsersQuery request, CancellationToken cancellationToken)
        {
            try
            {
                // 1. Database'den Entity'leri çek
                var userEntities = await GetUsersFromDatabaseAsync(cancellationToken);
                
                // 2. Entity'lerden DTO'ya dönüştür
                var userDtos = userEntities.Select(entity => new UserDto
                {
                    Id = entity.Id,
                    Name = entity.Name,
                    Email = entity.Email
                }).ToList();
                
                return new GetUsersResponse
                {
                    Users = userDtos,
                    TotalCount = userDtos.Count,
                    Success = true,
                    Message = "Users retrieved successfully"
                };
            }
            catch (Exception ex)
            {
                return new GetUsersResponse
                {
                    Success = false,
                    Message = ex.Message
                };
            }
        }

        // ✅ Bu kısım değişti - Entity oluşturuyoruz
        private async Task<List<User>> GetUsersFromDatabaseAsync(CancellationToken cancellationToken)
        {
            var users = new List<User>();
            
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync(cancellationToken);
            
            using var command = new SqlCommand("SELECT UID, UName, UEmail FROM Users", connection);
            using var reader = await command.ExecuteReaderAsync(cancellationToken);
            
            while (await reader.ReadAsync(cancellationToken))
            {
                // Entity oluştur (business logic ile)
                var user = new User(
                    id: Convert.ToInt32(reader["UID"]),
                    name: reader["UName"]?.ToString() ?? "",
                    email: reader["UEmail"]?.ToString() ?? ""
                );
                
                users.Add(user);
            }
            
            return users;
        }
    }
}