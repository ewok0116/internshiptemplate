// Features/OrderItems/GetOrderItems/GetOrderItemsController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace MyFoodOrderingAPI.Features.OrderItems.GetOrderItems
{
    [ApiController]
    [Route("api/orders/{orderId}/items")]
    public class GetOrderItemsController : ControllerBase
    {
        private readonly IMediator _mediator;

        public GetOrderItemsController(IMediator mediator)
        {
            _mediator = mediator;
        }

        [HttpGet]
        public async Task<IActionResult> GetOrderItems(int orderId)
        {
            var query = new GetOrderItemsQuery { OrderId = orderId };
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

        [HttpGet("{itemId}")]
        public async Task<IActionResult> GetOrderItem(int orderId, int itemId)
        {
            var query = new GetOrderItemQuery { OrderId = orderId, ItemId = itemId };
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