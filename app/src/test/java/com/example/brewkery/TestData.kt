package com.example.brewkery
import com.example.brewkery.data.model.Customizations
import com.example.brewkery.data.model.MenuItem
import com.example.brewkery.data.model.MilkOption
import com.example.brewkery.data.model.SizeOption

// Small helpers so tests don't repeat the long MenuItem constructor
fun fakeItem(id: Int, basePrice: Double) = MenuItem(
    id = id, categoryId = "cat", name = "Item $id", tagline = "", description = "",
    basePrice = basePrice, rating = 4.5, reviewCount = 1, prepTime = "", calories = 0,
    imageUrl = "", badge = "TEST", ingredients = emptyList(),
    customizations = Customizations(emptyList(), emptyList(), emptyList())
)

fun size(id: String, extra: Double) = SizeOption(id, id, extra)
fun milk(id: String, extra: Double) = MilkOption(id, id, extra)