package com.example.brewkery.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brewkery.data.model.Category
import com.example.brewkery.data.model.MenuItem
import com.example.brewkery.data.model.Meta
import com.example.brewkery.data.repository.CartRepository
import com.example.brewkery.data.repository.MenuRepository
import com.example.brewkery.domain.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** The three states the Home screen can be in (loading / error / success). */
sealed interface MenuUiState {
    data object Loading : MenuUiState
    data class Error(val message: String) : MenuUiState
    data class Success(
        val meta: Meta,
        val categories: List<Category>,
        val allItems: List<MenuItem>,
        val selectedCategoryId: String? = null, // null == "All Items"
        val query: String = ""
    ) : MenuUiState {
        /** Category chip + search box are combined here; the UI just renders this list. */
        val visibleItems: List<MenuItem>
            get() = allItems.filter {
                (selectedCategoryId == null || it.categoryId == selectedCategoryId) &&
                        (query.isBlank() || it.name.contains(query.trim(), ignoreCase = true))
            }
    }
}

class MenuViewModel(
    private val menuRepository: MenuRepository,
    cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    // Cart bar + active order card are derived from the shared CartRepository
    val cartCount: StateFlow<Int> = cartRepository.items
        .map { list -> list.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val cartSubtotal: StateFlow<Double> = cartRepository.items
        .map { list -> list.sumOf { it.lineTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val activeOrder: StateFlow<Order?> = cartRepository.activeOrder

    init { loadMenu() }

    fun loadMenu() {
        _uiState.value = MenuUiState.Loading
        viewModelScope.launch {
            menuRepository.getMenu().fold(
                onSuccess = { _uiState.value = MenuUiState.Success(it.meta, it.categories, it.items) },
                onFailure = { _uiState.value = MenuUiState.Error(it.toUserMessage()) }
            )
        }
    }

    fun onCategorySelected(id: String?) = updateSuccess { it.copy(selectedCategoryId = id) }
    fun onQueryChange(q: String) = updateSuccess { it.copy(query = q) }

    private fun updateSuccess(transform: (MenuUiState.Success) -> MenuUiState.Success) =
        _uiState.update { if (it is MenuUiState.Success) transform(it) else it }

/** Turn technical exceptions into friendly text. */
private fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException -> "No internet connection. Please check your network."
    is SocketTimeoutException -> "The request timed out. Please try again."
    is HttpException -> "Server error (${code()}). Please try again later."
    else -> "Something went wrong. Please try again."
}
}