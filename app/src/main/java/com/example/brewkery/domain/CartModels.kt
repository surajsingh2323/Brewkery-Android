package com.example.brewkery.domain


import com.example.brewkery.data.model.MenuItem
import com.example.brewkery.data.model.MilkOption
import com.example.brewkery.data.model.SizeOption

/** One line in the cart = item + chosen customizations + quantity. */
data class CartItem(
    val lineId: String,           // same item + same options => same line (quantity is merged)
    val item: MenuItem,
    val size: SizeOption,
    val milk: MilkOption,
    val sugar: String,
    val quantity: Int
) {
    val unitPrice: Double get() = PriceCalculator.unitPrice(item.basePrice, size.extraPrice, milk.extraPrice)
    val lineTotal: Double get() = PriceCalculator.round2(unitPrice * quantity)
}

enum class OrderStatus { PREPARING }

data class Order(
    val ticketId: String,
    val itemCount: Int,
    val estimatedWait: String,
    val status: OrderStatus = OrderStatus.PREPARING
)