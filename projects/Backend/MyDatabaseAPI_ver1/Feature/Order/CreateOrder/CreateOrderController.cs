// Features/Orders/CreateOrder/CreateOrderController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace MyFoodOrderingAPI.Features.Orders.CreateOrder
{
    [ApiController]
    [Route("api/orders")]
    public class CreateOrderController : ControllerBase
    {
        private readonly IMediator _mediator;

        public CreateOrderController(IMediator mediator)
        {
            _mediator = mediator;
        }

        [HttpPost]
        public async Task<IActionResult> CreateOrder([FromBody] CreateOrderCommand command)
        {
            var response = await _mediator.Send(command);
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