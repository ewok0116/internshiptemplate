
// domain/entities/PaymentMethod.kt
package com.example.foodorderingapp.domain.entities

enum class PaymentMethod(val displayName: String, val apiValue: String) {
    CREDIT_CARD("Credit Card", "Credit Card"),
    CASH("Cash", "Cash"),
    COUPON("Coupon", "Coupon"),
    SODEXO("Sodexo", "Sodexo"),
    MULTINET("Multinet", "Multinet"),
    EDENRED("Edenred", "Edenred")
}


enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    DELIVERED,
    CANCELLED
}