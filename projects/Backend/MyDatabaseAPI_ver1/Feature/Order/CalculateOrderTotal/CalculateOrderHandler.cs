
// Features/Orders/CalculateOrderTotal/CalculateOrderTotalHandler.cs
using MediatR;
using MyFoodOrderingAPI.Core.Services;
using MyFoodOrderingAPI.Core.Interfaces;

namespace MyFoodOrderingAPI.Features.Orders.CalculateOrderTotal
{
    public class CalculateOrderTotalHandler : IRequestHandler<CalculateOrderTotalCommand, CalculateOrderTotalResponse>
    {
        private readonly IOrderService _orderService;
        private readonly IProductRepository _productRepository;
        private readonly ILogger<CalculateOrderTotalHandler> _logger;

        public CalculateOrderTotalHandler(
            IOrderService orderService,
            IProductRepository productRepository,
            ILogger<CalculateOrderTotalHandler> logger)
        {
            _orderService = orderService;
            _productRepository = productRepository;
            _logger = logger;
        }

        public async Task<CalculateOrderTotalResponse> Handle(CalculateOrderTotalCommand request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("=== CalculateOrderTotalHandler Started ===");
                _logger.LogInformation("Calculating total for {ItemCount} items", request.Items.Count);

                if (!request.Items.Any())
                {
                    return new CalculateOrderTotalResponse
                    {
                        Success = false,
                        Message = "No items provided for calculation"
                    };
                }

                var calculatedItems = new List<CalculatedItemDto>();
                decimal subTotal = 0;

                // Calculate each item
                foreach (var item in request.Items)
                {
                    try
                    {
                        var product = await _productRepository.GetProductByIdAsync(item.ProductId, cancellationToken);
                        
                        if (product == null)
                        {
                            calculatedItems.Add(new CalculatedItemDto
                            {
                                ProductId = item.ProductId,
                                ProductName = $"Product {item.ProductId} (Not Found)",
                                Quantity = item.Quantity,
                                UnitPrice = 0,
                                TotalPrice = 0,
                                IsAvailable = false
                            });
                            continue;
                        }

                        var itemTotal = product.Price * item.Quantity;
                        
                        calculatedItems.Add(new CalculatedItemDto
                        {
                            ProductId = item.ProductId,
                            ProductName = product.Name,
                            Quantity = item.Quantity,
                            UnitPrice = product.Price,
                            TotalPrice = itemTotal,
                            IsAvailable = product.IsAvailable
                        });

                        if (product.IsAvailable)
                        {
                            subTotal += itemTotal;
                        }
                    }
                    catch (Exception ex)
                    {
                        _logger.LogWarning(ex, "Error processing item {ProductId}", item.ProductId);
                        calculatedItems.Add(new CalculatedItemDto
                        {
                            ProductId = item.ProductId,
                            ProductName = $"Product {item.ProductId} (Error)",
                            Quantity = item.Quantity,
                            UnitPrice = 0,
                            TotalPrice = 0,
                            IsAvailable = false
                        });
                    }
                }

                // Calculate tax
                var taxRate = request.TaxRate ?? 0.08m; // Default 8% tax
                var taxAmount = subTotal * taxRate;

                // Calculate delivery fee
                var deliveryFee = request.DeliveryFee ?? (subTotal > 50 ? 0 : 5.99m); // Free delivery over $50

                // Calculate discount (simplified coupon logic)
                decimal discountAmount = 0;
                bool couponApplied = false;
                
                if (!string.IsNullOrWhiteSpace(request.CouponCode))
                {
                    var (isValid, discount) = ApplyCoupon(request.CouponCode, subTotal);
                    if (isValid)
                    {
                        discountAmount = discount;
                        couponApplied = true;
                    }
                }

                var grandTotal = Math.Round(subTotal + taxAmount + deliveryFee - discountAmount, 2);

                _logger.LogInformation("✅ Calculation complete - SubTotal: {SubTotal}, Tax: {Tax}, Delivery: {Delivery}, Discount: {Discount}, Total: {Total}",
                    subTotal, taxAmount, deliveryFee, discountAmount, grandTotal);

                return new CalculateOrderTotalResponse
                {
                    Items = calculatedItems,
                    SubTotal = Math.Round(subTotal, 2),
                    TaxAmount = Math.Round(taxAmount, 2),
                    DeliveryFee = Math.Round(deliveryFee, 2),
                    DiscountAmount = Math.Round(discountAmount, 2),
                    GrandTotal = grandTotal,
                    CouponCode = request.CouponCode,
                    CouponApplied = couponApplied,
                    Success = true,
                    Message = $"Total calculated for {calculatedItems.Count} items"
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "❌ Critical error calculating order total");
                return new CalculateOrderTotalResponse
                {
                    Success = false,
                    Message = $"Critical error: {ex.Message}"
                };
            }
        }

        private (bool IsValid, decimal Discount) ApplyCoupon(string couponCode, decimal subTotal)
        {
            // Simplified coupon logic - in real app, this would query a database
            return couponCode.ToUpper() switch
            {
                "SAVE10" => (true, subTotal * 0.10m), // 10% off
                "SAVE5" => (true, 5.00m),             // $5 off
                "FREESHIP" => (true, 5.99m),          // Free shipping
                "WELCOME20" => (true, subTotal * 0.20m), // 20% off for new customers
                _ => (false, 0)
            };
        }
    }
}
