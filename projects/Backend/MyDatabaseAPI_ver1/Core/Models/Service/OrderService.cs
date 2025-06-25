using MyFoodOrderingAPI.Core.Entities;
using MyFoodOrderingAPI.Core.Interfaces;
using MyFoodOrderingAPI.Core.Services;
using MyFoodOrderingAPI.Core.Models; // ✅ ADDED: This import for CreateOrderRequest

namespace MyFoodOrderingAPI.Infrastructure.Services
{
    public class OrderService : IOrderService
    {
        private readonly IOrderRepository _orderRepository;
        private readonly IOrderItemRepository _orderItemRepository;
        private readonly IProductRepository _productRepository;
        private readonly IUserRepository _userRepository;
        private readonly ILogger<OrderService> _logger;

        public OrderService(
            IOrderRepository orderRepository,
            IOrderItemRepository orderItemRepository,
            IProductRepository productRepository,
            IUserRepository userRepository,
            ILogger<OrderService> logger)
        {
            _orderRepository = orderRepository;
            _orderItemRepository = orderItemRepository;
            _productRepository = productRepository;
            _userRepository = userRepository;
            _logger = logger;
        }

        public async Task<Order> CreateOrderAsync(CreateOrderRequest request, CancellationToken cancellationToken = default)
        {
            _logger.LogInformation("Creating order for user {UserId} with {ItemCount} items", 
                request.UserId, request.Items.Count);

            // 1. Validate user exists
            var user = await _userRepository.GetUserByIdAsync(request.UserId, cancellationToken);
            if (user == null)
                throw new ArgumentException($"User {request.UserId} not found");

            // 2. Validate request
            if (!request.IsValid())
                throw new ArgumentException("Invalid order request");

            // 3. Validate and get product details
            var productDetails = new Dictionary<int, (string Name, decimal Price)>();
            foreach (var item in request.Items)
            {
                var product = await _productRepository.GetProductByIdAsync(item.ProductId, cancellationToken);
                if (product == null)
                    throw new ArgumentException($"Product {item.ProductId} not found");
                
                if (!product.IsAvailable)
                    throw new ArgumentException($"Product {item.ProductId} is not available");

                productDetails[item.ProductId] = (product.Name, product.Price);
            }

            // 4. Calculate total amount
            var totalAmount = request.Items.Sum(item => 
                productDetails[item.ProductId].Price * item.Quantity);

            // 5. Create order entity
            var order = new Order(
                userId: request.UserId,
                totalAmount: Math.Round(totalAmount, 2),
                deliveryAddress: request.DeliveryAddress,
                paymentMethod: request.PaymentMethod
            );

            // 6. Save order to database
            var savedOrder = await _orderRepository.AddOrderAsync(order, cancellationToken);

            // 7. Create and save order items
            foreach (var itemRequest in request.Items)
            {
                var productPrice = productDetails[itemRequest.ProductId].Price;
                
                var orderItem = new OrderItem(
                    orderId: savedOrder.Id,
                    productId: itemRequest.ProductId,
                    quantity: itemRequest.Quantity,
                    unitPrice: productPrice
                );

                var savedOrderItem = await _orderItemRepository.AddOrderItemAsync(orderItem, cancellationToken);
                savedOrder.AddOrderItem(savedOrderItem);
            }

            _logger.LogInformation("Successfully created order {OrderId} for user {UserId} with total {TotalAmount}", 
                savedOrder.Id, request.UserId, savedOrder.TotalAmount);

            return savedOrder;
        }

        public async Task<Order?> GetOrderWithDetailsAsync(int orderId, CancellationToken cancellationToken = default)
        {
            return await _orderRepository.GetOrderWithItemsAsync(orderId, cancellationToken);
        }

        public async Task<bool> CancelOrderAsync(int orderId, int userId, CancellationToken cancellationToken = default)
        {
            var order = await _orderRepository.GetOrderByIdAsync(orderId, cancellationToken);
            
            if (order == null)
                return false;

            if (order.UserId != userId)
                throw new UnauthorizedAccessException("Order does not belong to user");

            if (!order.CanBeCancelled())
                throw new InvalidOperationException($"Order with status '{order.OrderStatus}' cannot be cancelled");

            order.UpdateStatus("Cancelled");
            await _orderRepository.UpdateOrderAsync(order, cancellationToken);

            _logger.LogInformation("Order {OrderId} cancelled by user {UserId}", orderId, userId);
            return true;
        }

        public async Task<Order> UpdateOrderStatusAsync(int orderId, string newStatus, CancellationToken cancellationToken = default)
        {
            var order = await _orderRepository.GetOrderByIdAsync(orderId, cancellationToken);
            
            if (order == null)
                throw new ArgumentException($"Order {orderId} not found");

            order.UpdateStatus(newStatus);
            var updatedOrder = await _orderRepository.UpdateOrderAsync(order, cancellationToken);

            _logger.LogInformation("Order {OrderId} status updated to {Status}", orderId, newStatus);
            return updatedOrder;
        }

        public async Task<decimal> CalculateOrderTotalAsync(List<OrderItemRequest> items, CancellationToken cancellationToken = default)
        {
            decimal total = 0;

            foreach (var item in items)
            {
                var product = await _productRepository.GetProductByIdAsync(item.ProductId, cancellationToken);
                if (product == null)
                    throw new ArgumentException($"Product {item.ProductId} not found");

                total += product.Price * item.Quantity;
            }

            return Math.Round(total, 2);
        }

        public async Task<bool> ValidateOrderItemsAsync(List<OrderItemRequest> items, CancellationToken cancellationToken = default)
        {
            if (!items.Any()) return false;

            foreach (var item in items)
            {
                if (item.Quantity <= 0) return false;

                var product = await _productRepository.GetProductByIdAsync(item.ProductId, cancellationToken);
                if (product == null || !product.IsAvailable) return false;
            }

            return true;
        }
    }
}