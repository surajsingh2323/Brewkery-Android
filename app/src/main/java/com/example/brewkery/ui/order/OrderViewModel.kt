package com.example.brewkery.ui.order
import androidx.lifecycle.ViewModel
import com.example.brewkery.data.repository.CartRepository
import com.example.brewkery.domain.Order
import kotlinx.coroutines.flow.StateFlow

class OrderViewModel(cartRepository: CartRepository) : ViewModel() {
    val order: StateFlow<Order?> = cartRepository.activeOrder
}
