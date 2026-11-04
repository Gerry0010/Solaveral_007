package com.solareval.app.ui.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta del prototipo de Figma
val Azul = Color(0xFF14233A)
val Sol = Color(0xFFF5A400)
val Gris = Color(0xFF4A5A70)
val Borde = Color(0xFFC9D3E0)
val Fondo = Color(0xFFF4F7FA)
val ErrorRojo = Color(0xFFB3261E)

@Composable
fun SolarevalTema(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Azul, onPrimary = Color.White,
            secondary = Sol, onSecondary = Azul,
            background = Color.White, onBackground = Azul,
            surface = Color.White, onSurface = Azul,
            error = ErrorRojo,
        ),
        content = content,
    )
}
