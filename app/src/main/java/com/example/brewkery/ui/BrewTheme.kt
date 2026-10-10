package com.example.brewkery.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.brewkery.R

/** Colours taken from the Figma screenshots. Change them here and the whole app updates. */
object BrewColors {
    val Background = Color(0xFFFFFAF6)
    val Dark = Color(0xFF140A06)
    val Accent = Color(0xFFD9552F)
    val AccentDark = Color(0xFF8E2F14)
    val AccentSoft = Color(0xFFFBE9DF)   // selected card background
    val Border = Color(0xFFEBD9CE)
    val Banner = Color(0xFFF6E7DC)
    val Badge = Color(0xFFFDEFBF)
    val BadgeText = Color(0xFF7A4B00)
    val Muted = Color(0xFF7A6A62)
    val Gold = Color(0xFFF5A623)
    val Danger = Color(0xFFE5174D)
    val Success = Color(0xFF0E8A5F)
}

/** The app font. Each Font(...) line links one file in res/font to a weight. */
val Jakarta = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold)
)

/*
 * Every Text() in the app uses the bodyLarge style by default.
 * Material's default adds wide letter spacing and tall line height, which made the
 * text look loose compared to the Figma. We switch to our font and remove both.
 */
private val base = Typography()
private val brewTypography = base.copy(
    bodyLarge = base.bodyLarge.copy(
        fontFamily = Jakarta,
        lineHeight = TextUnit.Unspecified,
        letterSpacing = 0.sp
    )
)

@Composable
fun BrewkeryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = BrewColors.Accent,
            background = BrewColors.Background,
            surface = BrewColors.Background,
            onBackground = BrewColors.Dark,
            onSurface = BrewColors.Dark
        ),
        typography = brewTypography,
        content = content
    )
}