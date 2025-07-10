
// data/mappers/OrderMapper.kt
package com.example.foodorderingapp_ver2.data.mappers

import com.example.foodorderingapp_ver2.domain.entities.*
import com.example.foodorderingapp_ver2.data.remote.*
import java.text.SimpleDateFormat
import java.util.*

object OrderMapper {
    fun mapToDomain(dto: CreateOrderResponseDto): Order {
        return Order(
            id = dto.orderId,
            userId = 0, // Not provided in response, could be stored separately
            items = dto.orderItems.map { mapOrderItemToDomain(it) },
            totalAmount = dto.totalAmount,
            paymentMethod = mapPaymentMethodToDomain(dto.paymentMethod),
            deliveryAddress = dto.deliveryAddress,
            orderDate = parseDate(dto.orderDate)
        )
    }

    fun mapCreateOrderRequestToDto(
        userId: Int,
        items: List<CartItem>,
        deliveryAddress: String,
        paymentMethod: PaymentMethod
    ): CreateOrderRequestDto {
        return CreateOrderRequestDto(
            userId = userId,
            deliveryAddress = deliveryAddress,
            paymentMethod = paymentMethod.apiValue,
            orderItems = items.map { cartItem ->
                CreateOrderItemRequestDto(
                    productId = cartItem.product.id,
                    quantity = cartItem.quantity
                )
            }
        )
    }

    private fun mapOrderItemToDomain(dto: OrderItemDto): OrderItem {
        return OrderItem(
            id = dto.id,
            productId = dto.productId,
            productName = dto.productName,
            quantity = dto.quantity,
            unitPrice = dto.unitPrice,
            totalPrice = dto.totalPrice
        )
    }


    private fun mapPaymentMethodToDomain(method: String): PaymentMethod {
        return PaymentMethod.values().find { it.apiValue == method }
            ?: PaymentMethod.CREDIT_CARD
    }

    private fun parseDate(dateString: String): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateString)
                ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}
