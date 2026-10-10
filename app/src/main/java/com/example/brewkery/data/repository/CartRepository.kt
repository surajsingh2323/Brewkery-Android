package com.example.brewkery.data.repository

import com.example.brewkery.data.model.MenuItem
import com.example.brewkery.data.model.MilkOption
import com.example.brewkery.data.model.SizeOption
import com.example.brewkery.domain.CartItem
import com.example.brewkery.domain.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * In-memory cart shared by all screens (one instance, owned by AppContainer).
 * The assignment says the cart may reset when the app closes, so no Room needed.
 * Exposes StateFlow so the UI always reacts to changes.
 */
class CartRepository(
    // Injectable so unit tests can get a predictable ticket id
    private val ticketIdGenerator: () -> String = { "BK-${Random.nextInt(10_000, 100_000)}" }
) {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    fun add(item: MenuItem, size: SizeOption, milk: MilkOption, sugar: String, quantity: Int) {
        val lineId = "${item.id}|${size.id}|${milk.id}|$sugar"
        _items.update { list ->
            if (list.any { it.lineId == lineId }) {
                // Same configuration already in cart -> just bump quantity
                list.map { if (it.lineId == lineId) it.copy(quantity = it.quantity + quantity) else it }
            } else {
                list + CartItem(lineId, item, size, milk, sugar, quantity)
            }
        }
    }

    /** delta = +1 / -1. Quantity reaching 0 removes the line. */
    fun changeQuantity(lineId: String, delta: Int) {
        _items.update { list ->
            list.mapNotNull {
                if (it.lineId != lineId) it
                else (it.quantity + delta).let { q -> if (q <= 0) null else it.copy(quantity = q) }
            }
        }
    }

    fun clear() { _items.value = emptyList() }

    /** Creates the ticket (status PREPARING), clears the cart, returns the order. */
    fun placeOrder(estimatedWait: String): Order? {
        val current = _items.value
        if (current.isEmpty()) return null
        val order = Order(
            ticketId = ticketIdGenerator(),
            itemCount = current.sumOf { it.quantity },
            estimatedWait = estimatedWait
        )
        _activeOrder.value = order
        _items.value = emptyList()
        return order
    }
}