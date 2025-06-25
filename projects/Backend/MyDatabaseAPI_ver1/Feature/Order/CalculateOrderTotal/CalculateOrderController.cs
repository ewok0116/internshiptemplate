// Features/Orders/CalculateOrderTotal/CalculateOrderTotalController.cs
using MediatR;
using Microsoft.AspNetCore.Mvc;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Features.Orders.CalculateOrderTotal
{
    [ApiController]
    [Route("api/calculate-order-total")]
    public class CalculateOrderTotalController : ControllerBase
    {
        private readonly IMediator _mediator;
        private readonly ILogger<CalculateOrderTotalController> _logger;

        public CalculateOrderTotalController(IMediator mediator, ILogger<CalculateOrderTotalController> logger)
        {
            _mediator = mediator;
            _logger = logger;
        }

        /// <summary>
        /// Calculate order total with tax, delivery, and discounts
        /// </summary>
        /// <param name="request">Calculation request</param>
        [HttpPost]
        public async Task<IActionResult> CalculateOrderTotal([FromBody] CalculateTotalRequest request)
        {
            try
            {
                _logger.LogInformation("CalculateOrderTotal endpoint called with {ItemCount} items", request.Items?.Count ?? 0);

                if (request?.Items == null || !request.Items.Any())
                {
                    return BadRequest(new { error = "Items are required for calculation" });
                }

                var command = new CalculateOrderTotalCommand
                {
                    Items = request.Items,
                    CouponCode = request.CouponCode,
                    TaxRate = request.TaxRate,
                    DeliveryFee = request.DeliveryFee
                };

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
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in CalculateOrderTotal endpoint");
                return StatusCode(500, new { error = "Internal server error", details = ex.Message });
            }
        }

        /// <summary>
        /// Get available coupon codes (for demonstration)
        /// </summary>
        [HttpGet("coupons")]
        public IActionResult GetAvailableCoupons()
        {
            var coupons = new[]
            {
                new { code = "SAVE10", description = "10% off your order", type = "percentage" },
                new { code = "SAVE5", description = "$5 off your order", type = "fixed" },
                new { code = "FREESHIP", description = "Free delivery", type = "shipping" },
                new { code = "WELCOME20", description = "20% off for new customers", type = "percentage" }
            };

            return Ok(new { coupons, message = "Available coupon codes" });
        }

        /// <summary>
        /// Health check
        /// </summary>
        [HttpGet("health")]
        public IActionResult HealthCheck()
        {
            return Ok(new { 
                status = "CalculateOrderTotal service is working!", 
                timestamp = DateTime.UtcNow 
            });
        }
    }

    public class CalculateTotalRequest
    {
        public List<OrderItemRequest> Items { get; set; } = new();
        public string? CouponCode { get; set; }
        public decimal? TaxRate { get; set; }
        public decimal? DeliveryFee { get; set; }
    }
}