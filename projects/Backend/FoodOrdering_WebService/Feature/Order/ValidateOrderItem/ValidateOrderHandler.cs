
// Features/Orders/ValidateOrderItems/ValidateOrderItemsHandler.cs
using MediatR;
using MyFoodOrderingAPI.Core.Services;
using MyFoodOrderingAPI.Core.Interfaces;

namespace MyFoodOrderingAPI.Features.Orders.ValidateOrderItems
{
    public class ValidateOrderItemsHandler : IRequestHandler<ValidateOrderItemsCommand, ValidateOrderItemsResponse>
    {
        private readonly IOrderService _orderService;
        private readonly IProductRepository _productRepository;
        private readonly ILogger<ValidateOrderItemsHandler> _logger;

        public ValidateOrderItemsHandler(
            IOrderService orderService,
            IProductRepository productRepository,
            ILogger<ValidateOrderItemsHandler> logger)
        {
            _orderService = orderService;
            _productRepository = productRepository;
            _logger = logger;
        }

        public async Task<ValidateOrderItemsResponse> Handle(ValidateOrderItemsCommand request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("=== ValidateOrderItemsHandler Started ===");
                _logger.LogInformation("Validating {ItemCount} items", request.Items.Count);

                if (!request.Items.Any())
                {
                    return new ValidateOrderItemsResponse
                    {
                        IsValid = false,
                        Success = false,
                        Message = "No items provided for validation"
                    };
                }

                var validationResults = new List<ItemValidationResult>();
                var errors = new List<string>();
                var warnings = new List<string>();
                int validItemCount = 0;

                // Check for duplicate products
                var duplicates = request.Items
                    .GroupBy(x => x.ProductId)
                    .Where(g => g.Count() > 1)
                    .Select(g => g.Key);

                if (duplicates.Any())
                {
                    errors.Add($"Duplicate products found: {string.Join(", ", duplicates)}");
                }

                // Validate each item
                foreach (var item in request.Items)
                {
                    var validationResult = await ValidateItem(item, request, cancellationToken);
                    validationResults.Add(validationResult);

                    if (validationResult.IsValid)
                    {
                        validItemCount++;
                    }

                    // Collect errors and warnings
                    foreach (var issue in validationResult.Issues)
                    {
                        if (validationResult.ProductExists && validationResult.IsAvailable)
                        {
                            warnings.Add($"Product {validationResult.ProductId}: {issue}");
                        }
                        else
                        {
                            errors.Add($"Product {validationResult.ProductId}: {issue}");
                        }
                    }
                }

                var isValid = validItemCount == request.Items.Count && !errors.Any();
                var invalidItemCount = request.Items.Count - validItemCount;

                _logger.LogInformation("✅ Validation complete - Valid: {Valid}/{Total}, Errors: {ErrorCount}, Warnings: {WarningCount}",
                    validItemCount, request.Items.Count, errors.Count, warnings.Count);

                return new ValidateOrderItemsResponse
                {
                    IsValid = isValid,
                    ValidationResults = validationResults,
                    Errors = errors,
                    Warnings = warnings,
                    ValidItemCount = validItemCount,
                    InvalidItemCount = invalidItemCount,
                    Success = true,
                    Message = isValid 
                        ? $"All {validItemCount} items are valid" 
                        : $"{validItemCount} valid, {invalidItemCount} invalid items found"
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "❌ Critical error validating order items");
                return new ValidateOrderItemsResponse
                {
                    IsValid = false,
                    Success = false,
                    Message = $"Critical error: {ex.Message}"
                };
            }
        }

        private async Task<ItemValidationResult> ValidateItem(
            MyFoodOrderingAPI.Core.Models.OrderItemRequest item, 
            ValidateOrderItemsCommand request, 
            CancellationToken cancellationToken)
        {
            var result = new ItemValidationResult
            {
                ProductId = item.ProductId,
                RequestedQuantity = item.Quantity
            };

            try
            {
                // Basic validation
                if (item.ProductId <= 0)
                {
                    result.Issues.Add("Invalid product ID");
                    return result;
                }

                if (item.Quantity <= 0)
                {
                    result.Issues.Add("Quantity must be greater than zero");
                    return result;
                }

                if (item.Quantity > 100)
                {
                    result.Issues.Add("Quantity exceeds maximum limit (100)");
                    return result;
                }

                // Get product details
                var product = await _productRepository.GetProductByIdAsync(item.ProductId, cancellationToken);
                
                if (product == null)
                {
                    result.Issues.Add("Product not found");
                    return result;
                }

                result.ProductExists = true;
                result.ProductName = product.Name;
                result.UnitPrice = product.Price;
                result.IsAvailable = product.IsAvailable;

                // Check availability
                if (request.CheckAvailability && !product.IsAvailable)
                {
                    result.Issues.Add("Product is not available");
                    return result;
                }

                // Check stock (simplified - in real app, this would check actual inventory)
                if (request.CheckStock)
                {
                    // Simulate stock check
                    var simulatedStock = GetSimulatedStock(item.ProductId);
                    result.AvailableStock = simulatedStock;
                    result.HasSufficientStock = simulatedStock >= item.Quantity;

                    if (!result.HasSufficientStock)
                    {
                        result.Issues.Add($"Insufficient stock. Available: {simulatedStock}, Requested: {item.Quantity}");
                        return result;
                    }
                }

                // If we get here, the item is valid
                result.IsValid = true;
                return result;
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Error validating item {ProductId}", item.ProductId);
                result.Issues.Add($"Validation error: {ex.Message}");
                return result;
            }
        }

        private int GetSimulatedStock(int productId)
        {
            // Simulate stock levels - in real app, this would query inventory database
            var random = new Random(productId); // Use productId as seed for consistent results
            return random.Next(0, 50); // Random stock between 0-50
        }
    }
}
