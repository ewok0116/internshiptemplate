namespace MyFoodOrderingAPI.Features.Users.GetUsers
{
    public class GetUsersResponse
    {
        public List<UserDto> Users { get; set; } = new();
        public int TotalCount { get; set; }
        public bool Success { get; set; } = true;
        public string Message { get; set; } = "";
    }

    public class UserDto
    {
        public int Id { get; set; }
        public string Name { get; set; } = "";
        public string Email { get; set; } = "";
    }
}