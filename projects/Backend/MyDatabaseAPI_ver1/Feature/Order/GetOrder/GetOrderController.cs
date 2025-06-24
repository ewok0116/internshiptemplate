// Features/Orders/GetOrders/GetOrdersController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace MyFoodOrderingAPI.Features.Orders.GetOrders
{
    [ApiController]
    [Route("api/orders")]
    public class GetOrdersController : ControllerBase
    {
        private readonly IMediator _mediator;

        public GetOrdersController(IMediator mediator)
        {
            _mediator = mediator;
        }

        [HttpGet]
        public async Task<IActionResult> GetOrders([FromQuery] GetOrdersQuery query)
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

        [HttpGet("{id}")]
        public async Task<IActionResult> GetOrderById(int id)
        {
            var query = new GetOrderByIdQuery { OrderId = id };
            var response = await _mediator.Send(query);
            if (response.Success)
            {
                return Ok(response);
            }
            else
            {
                return NotFound(new { error = response.Message });
            }
        }
    }
}