package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SwachhShikshaColorScheme = lightColorScheme(
    primary = BusYellow,
    onPrimary = AsphaltBlack,
    primaryContainer = BusYellowPale,
    onPrimaryContainer = AsphaltBlack,
    secondary = SkyBlue,
    onSecondary = PureWhite,
    secondaryContainer = SkyBluePale,
    onSecondaryContainer = AsphaltBlack,
    tertiary = BusOrange,
    onTertiary = PureWhite,
    background = PureWhite,
    onBackground = AsphaltBlack,
    surface = PureWhite,
    onSurface = AsphaltBlack,
    surfaceVariant = SkyBluePale,
    onSurfaceVariant = AsphaltSlate,
    error = AlertRed,
    onError = PureWhite
)

val CartoonShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SwachhShikshaColorScheme,
        typography = Typography,
        shapes = CartoonShapes,
        content = content
    )
}
