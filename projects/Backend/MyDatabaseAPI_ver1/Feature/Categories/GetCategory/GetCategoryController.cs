
// Features/Categories/GetCategories/GetCategoriesController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace MyFoodOrderingAPI.Features.Categories.GetCategories
{
    [ApiController]
    [Route("api/categories")]
    public class GetCategoriesController : ControllerBase
    {
        private readonly IMediator _mediator;

        public GetCategoriesController(IMediator mediator)
        {
            _mediator = mediator;
        }

        [HttpGet]
        public async Task<IActionResult> GetCategories([FromQuery] GetCategoriesQuery query)
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
