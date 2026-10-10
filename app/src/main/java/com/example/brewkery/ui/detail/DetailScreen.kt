package com.example.brewkery.ui.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brewkery.data.model.MilkOption
import com.example.brewkery.data.model.SizeOption
import com.example.brewkery.ui.BrewColors
import com.example.brewkery.ui.components.*
import com.example.brewkery.util.asExtraMoney
import com.example.brewkery.util.asMoney

@OptIn(ExperimentalLayoutApi::class) // FlowRow is still marked experimental
@Composable
fun DetailScreen(
    state: DetailUiState?,
    onBack: () -> Unit,
    onSize: (SizeOption) -> Unit,
    onMilk: (MilkOption) -> Unit,
    onSugar: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onAddToCart: () -> Unit
) {
    if (state == null) { // safety net: item missing from cache
        Column(Modifier.fillMaxSize().background(BrewColors.Background).statusBarsPadding().padding(16.dp)) {
            RoundBackButton(onBack)
            Text("Item not found", Modifier.padding(top = 24.dp), color = BrewColors.Muted)
        }
        return
    }
    val item = state.item

    Column(Modifier.fillMaxSize().background(BrewColors.Background).statusBarsPadding()) {
        // Scrollable content; the bottom bar below stays fixed
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---- Hero image with overlays ----
            Box(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp))) {
                BrewImage(item.imageUrl, Modifier.fillMaxSize())
                RoundBackButton(onBack, Modifier.align(Alignment.TopStart).padding(10.dp))
                Text(
                    item.badge, color = BrewColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                        .clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            // ---- Title + base price ----
            Row(verticalAlignment = Alignment.Top) {
                Text(item.name, Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(8.dp))
                Text(item.basePrice.asMoney(), color = BrewColors.Accent, fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
            }
            Text(item.description, color = BrewColors.Muted, fontSize = 13.sp, lineHeight = 19.sp)

            // ---- Ingredients (wrap onto multiple lines) ----
            Text("KEY INGREDIENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrewColors.Muted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp))  {
                item.ingredients.forEach {
                    Text(it, fontSize = 11.sp, color = BrewColors.Muted,
                        modifier = Modifier.border(1.dp, BrewColors.Border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            // ---- Dynamic customizations (driven 100% by the JSON) ----
            SectionCard("Size Selection") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.customizations.sizes.forEach { size ->
                        val selected = size.id == state.size.id
                        SelectableCard(selected, { onSize(size) }, Modifier.weight(1f)) {
                            Text(size.label, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = if (selected) BrewColors.Accent else BrewColors.Muted)
                            Text(size.extraPrice.asExtraMoney(), fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                                color = if (selected) BrewColors.Accent else BrewColors.Muted)
                        }
                    }
                }
            }

            SectionCard("Milk Options / Spreads") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.customizations.milkOptions.forEach { milk ->
                        val selected = milk.id == state.milk.id
                        SelectableCard(selected, { onMilk(milk) }, Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(milk.name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                    color = if (selected) BrewColors.Accent else BrewColors.Muted)
                                Text(milk.extraPrice.asExtraMoney(), fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                                    color = BrewColors.Muted)
                            }
                        }
                    }
                }
            }

            SectionCard("Sugar Levels / Serving") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.customizations.sugarLevels.forEach { level ->
                        val selected = level == state.sugar
                        Text(level, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = if (selected) Color.White else BrewColors.Muted,
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                .background(if (selected) BrewColors.Dark else Color.White)
                                .border(1.dp, BrewColors.Border, RoundedCornerShape(10.dp))
                                .clickable { onSugar(level) }
                                .padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
            }
        }

        // ---- Fixed bottom bar: quantity + Add to Cart with live price ----
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuantityStepper(state.quantity, onDecrement, onIncrement, Color.White)
            GradientButton(text = "", onClick = onAddToCart, modifier = Modifier.weight(1f)) {
                Text("Add to Cart  •  ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                // The price slides/fades when it changes => "price recalculation" feels alive
                AnimatedContent(
                    targetState = state.total,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                    },
                    label = "price"
                ) { total ->
                    Text(total.asMoney(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(Color.White).border(1.dp, BrewColors.Border, shape).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        content()
    }
}