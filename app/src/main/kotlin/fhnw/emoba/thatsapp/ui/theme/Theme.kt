package fhnw.emoba.modules.module04.beers_solution.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fhnw.emoba.thatsapp.ui.theme.amber500
import fhnw.emoba.thatsapp.ui.theme.gray900
import fhnw.emoba.thatsapp.ui.theme.teal200
import fhnw.emoba.thatsapp.ui.theme.tealA400

private val darkColors = darkColorScheme(
    //Background colors
    primary = Color(0xFF86FCAB),
    primaryContainer = Color(0xFF5B9B6F),
    secondary = Color(0xFF03DAC6),
    secondaryContainer = Color(0xFF03DAC6),
    background = Color(0xFF0C2422),
    surface = Color(0xFF273E3D),
    error = Color(0xFFCF6679),

    //Typography and icon colors
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black,
)


@Composable
fun MaterialAppTheme(content: @Composable() () -> Unit) {
    MaterialTheme(
        colorScheme = darkColors ,
        typography = typography,
        shapes = shapes,
        content = content
    )
}