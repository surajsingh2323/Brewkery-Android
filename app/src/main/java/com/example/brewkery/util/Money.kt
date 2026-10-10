package com.example.brewkery.util

import java.util.Locale

/** 5.5 -> "$5.50" (Locale.US so the decimal separator is always a dot) */
fun Double.asMoney(): String = "$" + String.format(Locale.US, "%.2f", this)

/** 0.65 -> "+$0.65" */
fun Double.asExtraMoney(): String = "+" + asMoney()