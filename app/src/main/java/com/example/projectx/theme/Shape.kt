package com.example.projectx.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Custom "Aura Campus" Shapes: 16dp Card, 12dp Button, 999dp Pill
val CardShape = RoundedCornerShape(16.dp)
val ButtonShape = RoundedCornerShape(12.dp)
val PillShape = RoundedCornerShape(999.dp)

val Shapes = Shapes(
    small = ButtonShape,
    medium = CardShape,
    large = PillShape
)
