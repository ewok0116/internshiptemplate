using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace MyFoodOrderingAPI.Features.Users.GetUsers
{
    [ApiController]
    [Route("api/users")]
    public class GetUsersController : ControllerBase
    {
        private readonly IMediator _mediator;

        public GetUsersController(IMediator mediator)
        {
            _mediator = mediator;
        }

        [HttpGet]
        public async Task<IActionResult> GetUsers([FromQuery] GetUsersQuery query)
        {
            var response = await _mediator.Send(query);
            
            if (response.Success)
            {
                return Ok(response);
            }
            else
            {
                return BadRequest(new { error = response.Message });
            }
        }
    }
}