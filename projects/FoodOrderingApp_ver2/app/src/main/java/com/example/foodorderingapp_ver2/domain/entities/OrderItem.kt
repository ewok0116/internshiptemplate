
data class OrderItem(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)