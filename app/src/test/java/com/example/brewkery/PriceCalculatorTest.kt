package com.example.brewkery
import com.example.brewkery.data.repository.CartRepository
import com.example.brewkery.domain.CartItem
import com.example.brewkery.domain.OrderStatus
import com.example.brewkery.domain.PriceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceCalculatorTest {

    @Test
    fun unitPrice_addsSizeAndMilkExtras() {
        // Grande (+0.65) and Oat (+0.00) on a $4.85 macchiato = $5.50
        assertEquals(5.50, PriceCalculator.unitPrice(4.85, 0.65, 0.0), 0.001)
    }

    @Test
    fun totals_matchDesignCart() {
        val macchiato = CartItem("a", fakeItem(1, 4.85), size("g", 0.65), milk("oat", 0.0), "0%", 1)
        val croissant = CartItem("b", fakeItem(3, 3.90), size("s", 0.0), milk("plain", 0.0), "warm", 1)

        val t = PriceCalculator.totals(listOf(macchiato, croissant), deliveryFee = 2.50, taxRatePercent = 8.0)

        assertEquals(9.40, t.subtotal, 0.001)
        assertEquals(2.50, t.deliveryFee, 0.001)
        assertEquals(0.75, t.tax, 0.001)   // 9.40 x 8% = 0.752, rounded to 0.75
        assertEquals(12.65, t.total, 0.001)
    }

    @Test
    fun totals_emptyCart_isZero() {
        val t = PriceCalculator.totals(emptyList(), 2.50, 8.0)
        assertEquals(0.0, t.total, 0.0)
    }

    @Test
    fun cart_mergesSameLines_andPlaceOrderClearsCart() {
        val repo = CartRepository(ticketIdGenerator = { "BK-12345" })
        val item = fakeItem(1, 4.85)
        repo.add(item, size("g", 0.65), milk("oat", 0.0), "0%", 1)
        repo.add(item, size("g", 0.65), milk("oat", 0.0), "0%", 2) // same options, so quantity merges

        assertEquals(1, repo.items.value.size)
        assertEquals(3, repo.items.value.first().quantity)

        val order = repo.placeOrder("20 - 30 mins")!!
        assertEquals("BK-12345", order.ticketId)
        assertEquals(3, order.itemCount)
        assertEquals(OrderStatus.PREPARING, order.status)
        assertTrue(repo.items.value.isEmpty())
    }

    @Test
    fun decrementToZero_removesLine() {
        val repo = CartRepository()
        repo.add(fakeItem(1, 4.0), size("s", 0.0), milk("m", 0.0), "x", 1)
        repo.changeQuantity(repo.items.value.first().lineId, -1)
        assertTrue(repo.items.value.isEmpty())
    }
}