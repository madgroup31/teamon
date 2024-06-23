package com.teamon.app.utils.graphics

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun generateMonogramLetters(name: String, surname: String): String {
    val firstInitial = if (name.isNotEmpty()) name[0].uppercaseChar() else ""
    val lastInitial = if (surname.isNotEmpty()) surname[0].uppercaseChar() else ""

    return "$firstInitial$lastInitial"
}

@Composable
fun Monogram(
    name: String,
    surname: String,
    color: ProjectColors,
    modifier: Modifier = Modifier,
) {
    Theme(color = color, applyToStatusBar = false) {

        var monogram: Bitmap? by remember { mutableStateOf(null) }

        val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        val contentColor = MaterialTheme.colorScheme.primary

        LaunchedEffect(name, surname) {
            withContext(Dispatchers.IO) {
                monogram = createMonogramBitmap(
                    name,
                    surname,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }
        }

        monogram?.let {
            Image(
                modifier = modifier,
                bitmap = it.asImageBitmap(),
                contentDescription = "Monogram"
            )// Draw the bitmap on the screen
        }
    }
}

private fun createMonogramBitmap(
    name: String,
    surname: String,
    containerColor: Color,
    contentColor: Color
): Bitmap {
    val letters = generateMonogramLetters(name, surname)
    val bitmap = Bitmap.createBitmap(
        400,
        400,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    canvas.drawColor(containerColor.toArgb())

    // Define the Paint attributes for the text
    val paint = Paint().asFrameworkPaint().apply {
        color = contentColor.toArgb()
        textSize = 120f // Adjust the text size as needed
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    }

    // Calculate the center coordinates
    val x = canvas.width / 2f
    val y = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2)

    // Draw the text on the canvas
    canvas.drawText(letters, x, y, paint)

    return bitmap
}