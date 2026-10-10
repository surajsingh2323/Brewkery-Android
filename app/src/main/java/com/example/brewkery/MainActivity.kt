package com.example.brewkery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.brewkery.ui.BrewkeryTheme
import com.example.brewkery.ui.cart.CartScreen
import com.example.brewkery.ui.cart.CartViewModel
import com.example.brewkery.ui.detail.DetailScreen
import com.example.brewkery.ui.detail.DetailViewModel
import com.example.brewkery.ui.menu.MenuScreen
import com.example.brewkery.ui.menu.MenuViewModel
import com.example.brewkery.ui.order.OrderScreen
import com.example.brewkery.ui.order.OrderViewModel
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { BrewkeryTheme { BrewkeryNavHost() } }
    }
}

private object Routes {
    const val MENU = "menu"
    const val DETAIL = "detail/{itemId}"
    const val CART = "cart"
    const val ORDER = "order"
    fun detail(id: Int) = "detail/$id"
}

@Composable
private fun BrewkeryNavHost() {
    val container = (LocalContext.current.applicationContext as BrewkeryApp).container
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.MENU,
        // Screen transitions: slide + fade in, gentle fade out
        enterTransition = { slideInHorizontally(tween(400, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(400)) },
        exitTransition = { slideOutHorizontally(tween(400, easing = FastOutSlowInEasing)) { -it / 3 } + fadeOut(tween(400)) },
        popEnterTransition = { slideInHorizontally(tween(400, easing = FastOutSlowInEasing)) { -it / 3 } + fadeIn(tween(400)) },
        popExitTransition = { slideOutHorizontally(tween(400, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(400)) }
    ) {
        // ---------- Screen 1: Menu ----------
        composable(Routes.MENU) {
            // viewModelFactory { initializer { } } = lightweight way to pass constructor args
            val vm: MenuViewModel = viewModel(factory = viewModelFactory {
                initializer { MenuViewModel(container.menuRepository, container.cartRepository) }
            })
            val state by vm.uiState.collectAsStateWithLifecycle()
            val cartCount by vm.cartCount.collectAsStateWithLifecycle()
            val subtotal by vm.cartSubtotal.collectAsStateWithLifecycle()
            val activeOrder by vm.activeOrder.collectAsStateWithLifecycle()

            MenuScreen(
                state = state, cartCount = cartCount, cartSubtotal = subtotal, activeOrder = activeOrder,
                onCategorySelected = vm::onCategorySelected,
                onQueryChange = vm::onQueryChange,
                onRetry = vm::loadMenu,
                onItemClick = { nav.navigate(Routes.detail(it)) },
                onCartClick = { nav.navigate(Routes.CART) },
                onOrderClick = { nav.navigate(Routes.ORDER) }
            )
        }
        // ---------- Screen 2: Item detail ----------
        composable(Routes.DETAIL, arguments = listOf(navArgument("itemId") { type = NavType.IntType })) { entry ->
            val itemId = entry.arguments?.getInt("itemId") ?: -1
            val vm: DetailViewModel = viewModel(factory = viewModelFactory {
                initializer { DetailViewModel(itemId, container.menuRepository, container.cartRepository) }
            })
            val state by vm.state.collectAsStateWithLifecycle()

            DetailScreen(
                state = state,
                onBack = { nav.popBackStack() },
                onSize = vm::selectSize, onMilk = vm::selectMilk, onSugar = vm::selectSugar,
                onIncrement = vm::increment, onDecrement = vm::decrement,
                onAddToCart = { vm.addToCart(); nav.popBackStack() } // back to menu, cart bar slides in
            )
        }

        // ---------- Screen 3: Cart ----------
        composable(Routes.CART) {
            val vm: CartViewModel = viewModel(factory = viewModelFactory {
                initializer { CartViewModel(container.cartRepository, container.menuRepository) }
            })
            val state by vm.uiState.collectAsStateWithLifecycle()

            CartScreen(
                state = state,
                onBack = { nav.popBackStack() },
                onClear = vm::clearCart,
                onQuantityChange = vm::changeQuantity,
                onPlaceOrder = {
                    if (vm.placeOrder()) {
                        // popUpTo(MENU) removes the cart from the back stack so Back from
                        // the order screen doesn't return to an empty cart
                        nav.navigate(Routes.ORDER) { popUpTo(Routes.MENU) }
                    }
                }
            )
        }
        // ---------- Screen 4: Order status ----------
        composable(Routes.ORDER) {
            val vm: OrderViewModel = viewModel(factory = viewModelFactory {
                initializer { OrderViewModel(container.cartRepository) }
            })
            val order by vm.order.collectAsStateWithLifecycle()
            OrderScreen(order = order, onBackToMenu = { nav.popBackStack(Routes.MENU, inclusive = false) })
        }
    }
}