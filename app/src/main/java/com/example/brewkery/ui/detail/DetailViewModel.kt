package com.example.brewkery.ui.detail

import androidx.lifecycle.ViewModel
import com.example.brewkery.data.model.MenuItem
import com.example.brewkery.data.model.MilkOption
import com.example.brewkery.data.model.SizeOption
import com.example.brewkery.data.repository.CartRepository
import com.example.brewkery.data.repository.MenuRepository
import com.example.brewkery.domain.PriceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Everything the detail screen needs. Prices are derived => recalculated automatically. */
data class DetailUiState(
    val item: MenuItem,
    val size: SizeOption,
    val milk: MilkOption,
    val sugar: String,
    val quantity: Int = 1
) {
    val unitPrice: Double get() = PriceCalculator.unitPrice(item.basePrice, size.extraPrice, milk.extraPrice)
    val total: Double get() = PriceCalculator.round2(unitPrice * quantity)
}

class DetailViewModel(
    itemId: Int,
    menuRepository: MenuRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    // null => item not found (e.g. process death cleared the in-memory cache)
    private val _state = MutableStateFlow(
        menuRepository.findItem(itemId)?.let {
            // Default selection = first option of each group, like the Figma
            DetailUiState(
                item = it,
                size = it.customizations.sizes.first(),
                milk = it.customizations.milkOptions.first(),
                sugar = it.customizations.sugarLevels.first()
            )
        }
    )
    val state: StateFlow<DetailUiState?> = _state.asStateFlow()

    fun selectSize(size: SizeOption) = update { it.copy(size = size) }
    fun selectMilk(milk: MilkOption) = update { it.copy(milk = milk) }
    fun selectSugar(sugar: String) = update { it.copy(sugar = sugar) }
    fun increment() = update { it.copy(quantity = it.quantity + 1) }
    fun decrement() = update { if (it.quantity > 1) it.copy(quantity = it.quantity - 1) else it }

    fun addToCart() {
        _state.value?.let { cartRepository.add(it.item, it.size, it.milk, it.sugar, it.quantity) }
    }

    private fun update(block: (DetailUiState) -> DetailUiState) =
        _state.update { current -> current?.let(block) }
}