// domain/usecases/CreateOrderUseCase.kt
package com.example.foodorderingapp_ver2.domain.usecases

import com.example.foodorderingapp_ver2.domain.entities.*
import com.example.foodorderingapp_ver2.domain.repositories.OrderRepository
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.domain.common.UseCase

data class CreateOrderParams(
    val userId: Int,
    val items: List<CartItem>,
    val deliveryAddress: String,
    val paymentMethod: PaymentMethod
)

class CreateOrderUseCase(
    private val orderRepository: OrderRepository
) : UseCase<CreateOrderParams, Order>() {

    override suspend fun invoke(parameters: CreateOrderParams): Result<Order> {
        // Validate cart items
        if (parameters.items.isEmpty()) {
            return Result.Error("Cart is empty")
        }

        // Validate payment method
        val allowedMethods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.EDENRED,
            PaymentMethod.SODEXO
        )

        if (!allowedMethods.contains(parameters.paymentMethod)) {
            return Result.Error("Payment method not accepted")
        }

        return orderRepository.createOrder(
            userId = parameters.userId,
            items = parameters.items,
            deliveryAddress = parameters.deliveryAddress,
            paymentMethod = parameters.paymentMethod
        )
    }
}
