package com.example.brewkery.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brewkery.domain.CartItem
import com.example.brewkery.domain.PriceCalculator
import com.example.brewkery.ui.BrewColors
import com.example.brewkery.ui.components.GradientButton
import com.example.brewkery.ui.components.QuantityStepper
import com.example.brewkery.ui.components.RoundBackButton
import com.example.brewkery.util.asMoney

@Composable
fun CartScreen(
    state: CartUiState,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onQuantityChange: (lineId: String, delta: Int) -> Unit,
    onPlaceOrder: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(BrewColors.Background).statusBarsPadding().padding(horizontal = 16.dp)) {

        // ---- Top bar ----
        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            RoundBackButton(onBack, Modifier.align(Alignment.CenterStart))
            Text("YOUR CART", Modifier.align(Alignment.Center), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Text("Clear Cart", Modifier.align(Alignment.CenterEnd).clickable(onClick = onClear),
                color = BrewColors.Danger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        if (state.items.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 40.sp)
                    Text("Your cart is empty", color = BrewColors.Muted, modifier = Modifier.padding(top = 8.dp))
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.items, key = { it.lineId }) { line ->
                    CartLine(line, Modifier.animateItem(), onQuantityChange)
                }
            }
        }

        // ---- Bill summary ----
        Summary(state.totals)
        Spacer(Modifier.height(12.dp))
        GradientButton(
            text = "Place Order Now  •  ${state.totals.total.asMoney()}",
            onClick = onPlaceOrder,
            enabled = state.items.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
        }
        Spacer(Modifier.navigationBarsPadding().height(16.dp))
    }
}
@Composable
private fun CartLine(line: CartItem, modifier: Modifier, onQuantityChange: (String, Int) -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier.fillMaxWidth().clip(shape).background(Color.White).border(1.dp, BrewColors.Border, shape).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(line.item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${line.size.label} • ${line.milk.name}", fontSize = 11.sp, color = BrewColors.Muted)
            Text(line.lineTotal.asMoney(), fontSize = 12.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace, color = BrewColors.Accent)
        }
        QuantityStepper(line.quantity, { onQuantityChange(line.lineId, -1) }, { onQuantityChange(line.lineId, +1) },
            background = BrewColors.Banner)
    }
}

@Composable
private fun Summary(t: PriceCalculator.Totals) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(Color.White).border(1.dp, BrewColors.Border, shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row2("Subtotal", t.subtotal.asMoney())
        Row2("Delivery Fee", t.deliveryFee.asMoney())
        Row2("Est. Tax (8.0%)", t.tax.asMoney())
        HorizontalDivider(color = BrewColors.Border)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Total Payable", Modifier.weight(1f), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(t.total.asMoney(), color = BrewColors.Accent, fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun Row2(label: String, value: String) {
    Row {
        Text(label, Modifier.weight(1f), color = BrewColors.Muted, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
    }
}