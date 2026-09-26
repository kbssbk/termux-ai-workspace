package com.beomsoo.sentencelibrary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = lightColorScheme(
    primary=Color(0xFF315EF5),
    onPrimary=Color.White,
    primaryContainer=Color(0xFFE8EEFF),
    onPrimaryContainer=Color(0xFF10275F),
    secondary=Color(0xFF315A78),
    background=Color(0xFFF7F9FC),
    surface=Color(0xFFFFFFFF),
    surfaceVariant=Color(0xFFEDF1F7),
    onSurface=Color(0xFF162033),
    onSurfaceVariant=Color(0xFF56647A),
    outline=Color(0xFFD7DEEA)
)

@Composable
fun SentenceLibraryTheme(content:@Composable ()->Unit) {
    MaterialTheme(colorScheme=Colors, typography=MaterialTheme.typography, content=content)
}
