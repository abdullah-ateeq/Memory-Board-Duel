package com.example.memoryboardduel.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Base = Typography()

val Typography = Typography(
    displayLarge = Base.displayLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
    displayMedium = Base.displayMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
    displaySmall = Base.displaySmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
    headlineLarge = Base.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp
    ),
    labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
    labelMedium = Base.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
    labelSmall = Base.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
)
