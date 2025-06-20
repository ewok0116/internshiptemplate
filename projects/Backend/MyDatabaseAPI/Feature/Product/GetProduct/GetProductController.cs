// Features/Products/GetProducts/GetProductsController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace MyFoodOrderingAPI.Features.Products.GetProducts
{
    [ApiController]
    [Route("api/products")]
    public class GetProductsController : ControllerBase
    {
        private readonly IMediator _mediator;

        public GetProductsController(IMediator mediator)
        {
            _mediator = mediator;
        }

        [HttpGet]
        public async Task<IActionResult> GetProducts([FromQuery] GetProductsQuery query)
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