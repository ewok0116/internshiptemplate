using MediatR;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Models;

namespace MyFoodOrderingAPI.Features.OrderItems.GetOrderItems
{
    public class GetOrderItemsHandler : IRequestHandler<GetOrderItemsQuery, GetOrderItemsResponse>
    {
        private readonly IOrderItemRepository _orderItemRepository;
        private readonly IOrderRepository _orderRepository;
        private readonly IProductRepository _productRepository;
        private readonly ILogger<GetOrderItemsHandler> _logger;

        public GetOrderItemsHandler(
            IOrderItemRepository orderItemRepository,
            IOrderRepository orderRepository,
            IProductRepository productRepository,
            ILogger<GetOrderItemsHandler> logger)
        {
            _orderItemRepository = orderItemRepository;
            _orderRepository = orderRepository;
            _productRepository = productRepository;
            _logger = logger;
        }

        public async Task<GetOrderItemsResponse> Handle(GetOrderItemsQuery request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("=== GetOrderItemsHandler Started ===");
                _logger.LogInformation("Retrieving order items for order {OrderId}", request.OrderId);

                // Validate input
                if (request.OrderId <= 0)
                {
                    _logger.LogWarning("Invalid OrderId: {OrderId}", request.OrderId);
                    return new GetOrderItemsResponse
                    {
                        Success = false,
                        Message = "Invalid order ID"
                    };
                }

                // Check if order exists first with detailed error handling
                _logger.LogInformation("Checking if order {OrderId} exists...", request.OrderId);
                
                try
                {
                    var order = await _orderRepository.GetOrderByIdAsync(request.OrderId, cancellationToken);
                    
                    if (order == null)
                    {
                        _logger.LogWarning("Order {OrderId} not found in database", request.OrderId);
                        return new GetOrderItemsResponse
                        {
                            Success = false,
                            Message = $"Order {request.OrderId} not found"
                        };
                    }

                    _logger.LogInformation("✅ Found order {OrderId}, Amount: {Amount}", 
                        order.Id, order.TotalAmount);
                }
                catch (Exception orderEx)
                {
                    _logger.LogError(orderEx, "❌ Failed to check if order {OrderId} exists", request.OrderId);
                    return new GetOrderItemsResponse
                    {
                        Success = false,
                        Message = $"Error checking order: {orderEx.Message}"
                    };
                }

                // Get order items with error handling
                _logger.LogInformation("Getting order items for order {OrderId}...", request.OrderId);
                List<MyFoodOrderingAPI.Core.Entities.OrderItem> orderItems;
                
                try
                {
                    orderItems = await _orderItemRepository.GetOrderItemsByOrderIdAsync(request.OrderId, cancellationToken);
                    _logger.LogInformation("✅ Found {Count} order items", orderItems.Count);
                }
                catch (Exception itemsEx)
                {
                    _logger.LogError(itemsEx, "❌ Failed to get order items for order {OrderId}", request.OrderId);
                    return new GetOrderItemsResponse
                    {
                        Success = false,
                        Message = $"Error retrieving order items: {itemsEx.Message}"
                    };
                }

                if (orderItems.Count == 0)
                {
                    _logger.LogInformation("No order items found for order {OrderId}", request.OrderId);
                    return new GetOrderItemsResponse
                    {
                        OrderItems = new List<OrderItemDetailDto>(),
                        TotalAmount = 0,
                        TotalItems = 0,
                        Success = true,
                        Message = $"No items found for order {request.OrderId}"
                    };
                }

                // Build DTOs with product details - process each item individually
                var orderItemDtos = new List<OrderItemDetailDto>();
                
                for (int i = 0; i < orderItems.Count; i++)
                {
                    var orderItem = orderItems[i];
                    _logger.LogInformation("Processing order item {Index}/{Total} - ItemId: {ItemId}, ProductId: {ProductId}", 
                        i + 1, orderItems.Count, orderItem.Id, orderItem.ProductId);

                    try
                    {
                        // Get product details with error handling
                        string productName = "";
                        string productDescription = "";
                        string productImageUrl = "";
                        
                        try
                        {
                            _logger.LogDebug("Getting product {ProductId}...", orderItem.ProductId);
                            var product = await _productRepository.GetProductByIdAsync(orderItem.ProductId, cancellationToken);
                            
                            if (product != null)
                            {
                                productName = product.Name ?? $"Product {orderItem.ProductId}";
                                productDescription = product.Description ?? "";
                                productImageUrl = product.ImageUrl ?? "";
                                _logger.LogDebug("✅ Found product: {ProductName}", productName);
                            }
                            else
                            {
                                _logger.LogWarning("Product {ProductId} not found, using fallback", orderItem.ProductId);
                                productName = $"Product {orderItem.ProductId}";
                            }
                        }
                        catch (Exception productEx)
                        {
                            _logger.LogWarning(productEx, "❌ Failed to get product {ProductId}, using fallback", orderItem.ProductId);
                            productName = $"Product {orderItem.ProductId}";
                        }

                        var orderItemDto = new OrderItemDetailDto
                        {
                            Id = orderItem.Id,
                            OrderId = orderItem.OrderId,
                            ProductId = orderItem.ProductId,
                            ProductName = productName,
                            ProductDescription = productDescription,
                            ProductImageUrl = productImageUrl,
                            Quantity = orderItem.Quantity,
                            UnitPrice = orderItem.UnitPrice,
                            TotalPrice = orderItem.TotalPrice,
                            IsValid = orderItem.IsValid
                        };

                        orderItemDtos.Add(orderItemDto);
                        _logger.LogDebug("✅ Successfully processed order item {ItemId}", orderItem.Id);
                    }
                    catch (Exception itemEx)
                    {
                        _logger.LogError(itemEx, "❌ Failed to process order item {ItemId}, skipping", orderItem.Id);
                        // Continue with next item instead of failing completely
                    }
                }

                // Calculate totals safely
                var totalAmount = orderItemDtos.Sum(x => x.TotalPrice);
                var totalItems = orderItemDtos.Sum(x => x.Quantity);

                _logger.LogInformation("✅ Successfully processed {ProcessedCount}/{TotalCount} order items. Total: ${TotalAmount}, Items: {TotalItems}", 
                    orderItemDtos.Count, orderItems.Count, totalAmount, totalItems);

                return new GetOrderItemsResponse
                {
                    OrderItems = orderItemDtos,
                    TotalAmount = totalAmount,
                    TotalItems = totalItems,
                    Success = true,
                    Message = $"Found {orderItemDtos.Count} order items for order {request.OrderId}"
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "❌ Critical error retrieving order items for order {OrderId}", request.OrderId);
                return new GetOrderItemsResponse
                {
                    Success = false,
                    Message = $"Critical error: {ex.Message}"
                };
            }
        }
    }

    // You'll also need the GetOrderItemHandler for individual items
    public class GetOrderItemHandler : IRequestHandler<GetOrderItemQuery, GetOrderItemResponse>
    {
        private readonly IOrderItemRepository _orderItemRepository;
        private readonly IOrderRepository _orderRepository;
        private readonly IProductRepository _productRepository;
        private readonly ILogger<GetOrderItemHandler> _logger;

        public GetOrderItemHandler(
            IOrderItemRepository orderItemRepository,
            IOrderRepository orderRepository,
            IProductRepository productRepository,
            ILogger<GetOrderItemHandler> logger)
        {
            _orderItemRepository = orderItemRepository;
            _orderRepository = orderRepository;
            _productRepository = productRepository;
            _logger = logger;
        }

        public async Task<GetOrderItemResponse> Handle(GetOrderItemQuery request, CancellationToken cancellationToken)
        {
            try
            {
                _logger.LogInformation("=== GetOrderItemHandler Started ===");
                _logger.LogInformation("Retrieving order item {ItemId} for order {OrderId}", request.ItemId, request.OrderId);

                // Validate input
                if (request.OrderId <= 0 || request.ItemId <= 0)
                {
                    _logger.LogWarning("Invalid IDs - OrderId: {OrderId}, ItemId: {ItemId}", request.OrderId, request.ItemId);
                    return new GetOrderItemResponse
                    {
                        Success = false,
                        Message = "Invalid order ID or item ID"
                    };
                }

                // Check if order exists
                _logger.LogInformation("Checking if order {OrderId} exists...", request.OrderId);
                var order = await _orderRepository.GetOrderByIdAsync(request.OrderId, cancellationToken);
                
                if (order == null)
                {
                    _logger.LogWarning("Order {OrderId} not found", request.OrderId);
                    return new GetOrderItemResponse
                    {
                        Success = false,
                        Message = $"Order {request.OrderId} not found"
                    };
                }

                // Get the specific order item
                _logger.LogInformation("Getting order item {ItemId}...", request.ItemId);
                var orderItem = await _orderItemRepository.GetOrderItemByIdAsync(request.ItemId, cancellationToken);
                
                if (orderItem == null)
                {
                    _logger.LogWarning("Order item {ItemId} not found", request.ItemId);
                    return new GetOrderItemResponse
                    {
                        Success = false,
                        Message = $"Order item {request.ItemId} not found"
                    };
                }

                // Verify the item belongs to the specified order
                if (orderItem.OrderId != request.OrderId)
                {
                    _logger.LogWarning("Order item {ItemId} does not belong to order {OrderId}", request.ItemId, request.OrderId);
                    return new GetOrderItemResponse
                    {
                        Success = false,
                        Message = $"Order item {request.ItemId} does not belong to order {request.OrderId}"
                    };
                }

                // Get product details with error handling
                string productName = "";
                string productDescription = "";
                string productImageUrl = "";
                
                try
                {
                    var product = await _productRepository.GetProductByIdAsync(orderItem.ProductId, cancellationToken);
                    if (product != null)
                    {
                        productName = product.Name ?? $"Product {orderItem.ProductId}";
                        productDescription = product.Description ?? "";
                        productImageUrl = product.ImageUrl ?? "";
                    }
                    else
                    {
                        productName = $"Product {orderItem.ProductId}";
                    }
                }
                catch (Exception productEx)
                {
                    _logger.LogWarning(productEx, "Failed to get product {ProductId}, using fallback", orderItem.ProductId);
                    productName = $"Product {orderItem.ProductId}";
                }

                var orderItemDto = new OrderItemDetailDto
                {
                    Id = orderItem.Id,
                    OrderId = orderItem.OrderId,
                    ProductId = orderItem.ProductId,
                    ProductName = productName,
                    ProductDescription = productDescription,
                    ProductImageUrl = productImageUrl,
                    Quantity = orderItem.Quantity,
                    UnitPrice = orderItem.UnitPrice,
                    TotalPrice = orderItem.TotalPrice,
                    IsValid = orderItem.IsValid
                };

                _logger.LogInformation("✅ Successfully retrieved order item {ItemId}", request.ItemId);

                return new GetOrderItemResponse
                {
                    OrderItem = orderItemDto,
                    Success = true,
                    Message = "Order item found successfully"
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "❌ Critical error retrieving order item {ItemId} for order {OrderId}", request.ItemId, request.OrderId);
                return new GetOrderItemResponse
                {
                    Success = false,
                    Message = $"Critical error: {ex.Message}"
                };
            }
        }
    }
}