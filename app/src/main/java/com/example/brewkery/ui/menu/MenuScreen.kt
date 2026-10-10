package com.example.brewkery.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brewkery.data.model.Category
import com.example.brewkery.data.model.MenuItem
import com.example.brewkery.data.model.Meta
import com.example.brewkery.domain.Order
import com.example.brewkery.ui.BrewColors
import com.example.brewkery.ui.components.BadgePill
import com.example.brewkery.ui.components.BrewImage
import com.example.brewkery.ui.components.ErrorView
import com.example.brewkery.ui.components.LoadingView
import com.example.brewkery.util.asMoney

/**
 * Home screen. It only displays what the ViewModel gives it and
 * passes taps back up through the lambdas (no logic in here).
 */
@Composable
fun MenuScreen(
    state: MenuUiState,
    cartCount: Int,
    cartSubtotal: Double,
    activeOrder: Order?,
    onCategorySelected: (String?) -> Unit,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onOrderClick: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(BrewColors.Background).statusBarsPadding()) {
        when (state) {
            MenuUiState.Loading -> LoadingView()
            is MenuUiState.Error -> ErrorView(state.message, onRetry)
            is MenuUiState.Success -> MenuContent(
                state, activeOrder, cartCount, onCategorySelected, onQueryChange,
                onItemClick, onCartClick, onOrderClick
            )
        }

        // The cart bar slides up from the bottom when the first item is added
        AnimatedVisibility(
            visible = cartCount > 0,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            CartBar(cartCount, cartSubtotal, onCartClick)
        }
    }
}

@Composable
private fun MenuContent(
    state: MenuUiState.Success,
    activeOrder: Order?,
    cartCount: Int,
    onCategorySelected: (String?) -> Unit,
    onQueryChange: (String) -> Unit,
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onOrderClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Extra space at the bottom so the last card is not hidden behind the cart bar
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Header(cartCount, onCartClick) }
        item { StoreBanner(state.meta) }
        // Shown only after an order has been placed
        if (activeOrder != null) item { ActiveOrderCard(activeOrder, onOrderClick) }
        item { SearchField(state.query, onQueryChange) }
        item { CategoryRow(state.categories, state.selectedCategoryId, onCategorySelected) }

        val visible = state.visibleItems
        if (visible.isEmpty()) {
            item { Text("No items found", color = BrewColors.Muted, modifier = Modifier.padding(16.dp)) }
        }
        // The key lets animateItem() animate cards moving when you filter or search
        items(visible, key = { it.id }) { item ->
            MenuItemCard(item, Modifier.animateItem()) { onItemClick(item.id) }
        }
    }
}

@Composable
private fun Header(cartCount: Int, onCartClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(BrewColors.Dark), contentAlignment = Alignment.Center) {
            Text("BK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Fresh Roast & Bakes", fontSize = 11.sp, color = BrewColors.Muted)
            Text("Brewkery Artisans", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(Color.White)
                .border(1.dp, BrewColors.Border, CircleShape).clickable(onClick = onCartClick),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(badge = {
                if (cartCount > 0) Badge(containerColor = BrewColors.Accent) { Text(cartCount.toString()) }
            }) { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun StoreBanner(meta: Meta) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(BrewColors.Banner)
            .border(1.dp, BrewColors.Border, shape).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.White), contentAlignment = Alignment.Center) {
            Text("🛵", fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("STORE INFO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrewColors.Accent)
                Spacer(Modifier.width(4.dp))
                Box(Modifier.size(5.dp).clip(CircleShape).background(BrewColors.Accent))
            }
            Text("Delivery in ${meta.estimatedDeliveryTime}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${meta.deliveryFee.asMoney()} flat fee", fontSize = 11.sp, color = BrewColors.Muted)
        }
        Text(
            "Open", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White)
                .border(1.dp, BrewColors.Border, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ActiveOrderCard(order: Order, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(Color.White)
            .border(1.dp, BrewColors.Accent, shape).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("☕", fontSize = 22.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Order #${order.ticketId}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Preparing • ${order.estimatedWait}", fontSize = 12.sp, color = BrewColors.Muted)
        }
        Text("Track →", color = BrewColors.Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(Icons.Default.Search, null, tint = BrewColors.Muted) },
        placeholder = { Text("Search roast, cold brew, pastry...", color = BrewColors.Muted, fontSize = 14.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            focusedBorderColor = BrewColors.Accent, unfocusedBorderColor = BrewColors.Border
        )
    )
}

@Composable
private fun CategoryRow(categories: List<Category>, selectedId: String?, onSelect: (String?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { CategoryChip("All Items", selectedId == null) { onSelect(null) } }
        items(categories, key = { it.id }) { c ->
            CategoryChip("${c.icon} ${c.name}", selectedId == c.id) { onSelect(c.id) }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    // The colours fade smoothly when the selected chip changes
    val bg by animateColorAsState(if (selected) BrewColors.Dark else Color.White, label = "chipBg")
    val fg by animateColorAsState(if (selected) Color.White else BrewColors.Muted, label = "chipFg")
    Text(
        label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(CircleShape).background(bg)
            .border(1.dp, BrewColors.Border, CircleShape).clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    )
}

@Composable
private fun MenuItemCard(item: MenuItem, modifier: Modifier = Modifier, onCustomize: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier
            .fillMaxWidth()
            .shadow(3.dp, shape, ambientColor = Color(0x228E2F14), spotColor = Color(0x228E2F14)) // soft shadow
            .clip(shape)
            .background(Color.White)
            .border(1.dp, BrewColors.Border, shape)
            .clickable(onClick = onCustomize)
            .padding(10.dp)
    ) {
        BrewImage(item.imageUrl, Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            BadgePill(item.badge)
            Spacer(Modifier.height(3.dp))
            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("⭐ ${item.rating} (${item.reviewCount})", fontSize = 11.sp, color = BrewColors.Muted)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.basePrice.asMoney(), color = BrewColors.Accent, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "+ Customize", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(BrewColors.Accent)
                        .clickable(onClick = onCustomize).padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun CartBar(count: Int, subtotal: Double, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)
            .clip(RoundedCornerShape(20.dp)).background(BrewColors.Dark)
            .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(BrewColors.Accent), contentAlignment = Alignment.Center) {
            Text(count.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("View Your Cart", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            Text(subtotal.asMoney(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Text("Proceed to Checkout →", color = BrewColors.Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}