using MediatR;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Features.Users.GetUsers
{
    public class GetUsersHandler : IRequestHandler<GetUsersQuery, GetUsersResponse>
    {
        private readonly IUserRepository _userRepository;
        private readonly ILogger<GetUsersHandler> _logger;

        public GetUsersHandler(
            IUserRepository userRepository,
            ILogger<GetUsersHandler> logger)
        {
            _userRepository = userRepository;
            _logger = logger;
        }

        public async Task<GetUsersResponse> Handle(GetUsersQuery request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("Starting to retrieve users with search term: {SearchTerm}, page: {PageNumber}", 
                    request.SearchTerm, request.PageNumber);

                // Create filter from request
                var filter = new UserFilter
                {
                    SearchTerm = request.SearchTerm,
                    PageNumber = request.PageNumber,
                    PageSize = request.PageSize
                };

                _logger.LogInformation("Created filter, calling repository...");

                // Use repository instead of direct SQL
                var userEntities = await _userRepository.GetUsersAsync(filter, cancellationToken);
                
                _logger.LogInformation("Got {Count} users from repository", userEntities.Count);
                
                var totalCount = await _userRepository.GetUsersCountAsync(filter, cancellationToken);
                
                _logger.LogInformation("Total count: {TotalCount}", totalCount);

                // Map entities to DTOs
                var userDtos = userEntities.Select(entity => new UserDto
                {
                    Id = entity.Id,
                    Name = entity.Name,
                    Email = entity.Email
                }).ToList();

                _logger.LogInformation("Successfully retrieved {Count} users out of {TotalCount} total", 
                    userDtos.Count, totalCount);

                return new GetUsersResponse
                {
                    Data = userDtos,
                    TotalCount = totalCount,
                    Success = true,
                    Message = "Users retrieved successfully"
                };
            }
            catch (Exception ex)
            {
                // 🎯 DETAILED ERROR LOGGING
                _logger.LogError(ex, "DETAILED ERROR: {Message}. StackTrace: {StackTrace}", 
                    ex.Message, ex.StackTrace);
                
                return new GetUsersResponse
                {
                    Success = false,
                    Message = $"Error: {ex.Message}" // 🎯 Return actual error message for debugging
                };
            }
        }
    }
}