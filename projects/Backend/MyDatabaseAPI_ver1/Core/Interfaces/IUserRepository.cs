using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Models; 
namespace MyFoodOrderingAPI.Core.Interfaces
{
    public interface IUserRepository
    {
        Task<List<User>> GetUsersAsync(UserFilter filter, CancellationToken cancellationToken = default);
        Task<User?> GetUserByIdAsync(int id, CancellationToken cancellationToken = default);
        Task<User?> GetUserByEmailAsync(string email, CancellationToken cancellationToken = default);
        Task<User> AddUserAsync(User user, CancellationToken cancellationToken = default);
        Task<User> UpdateUserAsync(User user, CancellationToken cancellationToken = default);
        Task<bool> DeleteUserAsync(int id, CancellationToken cancellationToken = default);
        Task<int> GetUsersCountAsync(UserFilter filter, CancellationToken cancellationToken = default);
    }
}