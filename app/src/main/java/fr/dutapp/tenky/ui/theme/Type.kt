package fr.dutapp.tenky.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal val TenkyTypography = Typography().let { base ->
    base.copy(
        // The current temperature is the focal point of the weather screen.
        displayLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Thin,
            fontSize = 96.sp,
            lineHeight = 100.sp,
        ),
    )
}
