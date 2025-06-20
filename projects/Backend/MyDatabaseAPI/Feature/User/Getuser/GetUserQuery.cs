using MediatR;

namespace MyFoodOrderingAPI.Features.Users.GetUsers
{
    public class GetUsersQuery : IRequest<GetUsersResponse>
    {
        public string? SearchTerm { get; set; }
        public int PageNumber { get; set; } = 1;
        public int PageSize { get; set; } = 10;
    }
}