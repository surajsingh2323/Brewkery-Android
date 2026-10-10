package com.example.brewkery.domain

import kotlin.math.roundToLong

/** Pure Kotlin object (no Android imports) => trivial to unit test. */
object PriceCalculator {

    data class Totals(
        val subtotal: Double,
        val deliveryFee: Double,
        val tax: Double,
        val total: Double
    )

    fun round2(value: Double): Double = (value * 100).roundToLong() / 100.0

    /** base + size extra + milk/spread extra */
    fun unitPrice(base: Double, sizeExtra: Double, milkExtra: Double): Double =
        round2(base + sizeExtra + milkExtra)

    /**
     * Subtotal + flat delivery fee + tax.
     * Tax is applied on the SUBTOTAL only (matches the Figma: 9.40 * 8% = 0.75).
     * Empty cart => everything is zero so we don't charge delivery for nothing.
     */
    fun totals(items: List<CartItem>, deliveryFee: Double, taxRatePercent: Double): Totals {
        if (items.isEmpty()) return Totals(0.0, 0.0, 0.0, 0.0)
        val subtotal = round2(items.sumOf { it.lineTotal })
        val tax = round2(subtotal * taxRatePercent / 100.0)
        return Totals(subtotal, deliveryFee, tax, round2(subtotal + deliveryFee + tax))
    }
}