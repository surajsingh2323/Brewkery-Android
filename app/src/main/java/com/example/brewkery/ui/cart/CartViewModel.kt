package com.example.brewkery.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewkery.data.repository.CartRepository
import com.example.brewkery.data.repository.MenuRepository
import com.example.brewkery.domain.CartItem
import com.example.brewkery.domain.PriceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totals: PriceCalculator.Totals = PriceCalculator.Totals(0.0, 0.0, 0.0, 0.0)
)

class CartViewModel(
    private val cartRepository: CartRepository,
    private val menuRepository: MenuRepository
) : ViewModel() {

    // Fees come from the API's meta block; fall back to the documented values if missing.
    private val meta get() = menuRepository.cachedMenu?.meta
    private val deliveryFee get() = meta?.deliveryFee ?: 2.50
    private val taxRate get() = meta?.taxRatePercent ?: 8.0

    val uiState: StateFlow<CartUiState> = cartRepository.items
        .map { CartUiState(it, PriceCalculator.totals(it, deliveryFee, taxRate)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun changeQuantity(lineId: String, delta: Int) = cartRepository.changeQuantity(lineId, delta)
    fun clearCart() = cartRepository.clear()

    /** Returns true if an order was created, so the UI knows to navigate. */
    fun placeOrder(): Boolean =
        cartRepository.placeOrder(meta?.estimatedDeliveryTime ?: "20 - 30 mins") != null
}