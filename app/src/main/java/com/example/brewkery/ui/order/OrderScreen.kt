package com.example.brewkery.ui.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brewkery.domain.Order
import com.example.brewkery.ui.BrewColors

@Composable
fun OrderScreen(order: Order?, onBackToMenu: () -> Unit) {
    // Flip once on first composition to trigger the entrance animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Endless gentle "breathing" of the ring around the cup
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )
    Column(
        Modifier.fillMaxSize().background(BrewColors.Background).statusBarsPadding().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        AnimatedVisibility(visible, enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) {
            Box(
                Modifier.size(80.dp).scale(pulse).clip(CircleShape).background(Color.White)
                    .border(2.dp, BrewColors.Accent, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("☕", fontSize = 32.sp) }
        }
        Spacer(Modifier.height(12.dp))
        Text("ORDER DISPATCHED", color = BrewColors.Accent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Text("Brewing in Progress!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text("Your ticket was dispatched to our barista.", color = BrewColors.Muted, fontSize = 13.sp)

        Spacer(Modifier.weight(1f))

        if (order != null) {
            AnimatedVisibility(visible, enter = slideInVertically(tween(500)) { it / 2 } + fadeIn(tween(500))) {
                TicketCard(order)
            }
        }

        Spacer(Modifier.weight(1f))
        Text(
            "Back to Menu", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BrewColors.Dark)
                .clickable(onClick = onBackToMenu).padding(vertical = 16.dp)
        )
        Spacer(Modifier.navigationBarsPadding().height(16.dp))
    }
}

@Composable
private fun TicketCard(order: Order) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(Color.White).border(1.dp, BrewColors.Border, shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ORDER TICKET", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BrewColors.Muted)
                Text("#${order.ticketId}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
            // status enum -> readable label (PREPARING)
            Text(
                order.status.name, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = BrewColors.BadgeText,
                modifier = Modifier.clip(CircleShape).background(BrewColors.Badge).padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        HorizontalDivider(color = BrewColors.Border)
        InfoRow("Estimated Wait:", order.estimatedWait, BrewColors.Accent)
        InfoRow("Items Ordered:", "${order.itemCount} Items", BrewColors.Dark)
        HorizontalDivider(color = BrewColors.Border)
        Text("Status:", fontSize = 11.sp, color = BrewColors.Muted)
        Text("Barista accepted your order!", color = BrewColors.Success, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color) {
    Row {
        Text(label, Modifier.weight(1f), color = BrewColors.Muted, fontSize = 14.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}