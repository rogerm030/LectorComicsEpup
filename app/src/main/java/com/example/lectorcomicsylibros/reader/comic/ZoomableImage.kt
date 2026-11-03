package com.example.lectorcomicsylibros.reader.comic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ZoomableImage(
    file: File,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Reset zoom and offset when image file changes
    LaunchedEffect(file) {
        scale = 1f
        offset = Offset.Zero
    }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 200),
        label = "ZoomScale"
    )

    val transformModifier = if (scale > 1.01f) {
        Modifier.pointerInput(file, scale) {
            detectTransformGestures { _, pan, zoom, _ ->
                val newScale = (scale * zoom).coerceIn(1f, 5f)
                scale = newScale

                if (newScale > 1f) {
                    val maxOffsetX = (newScale - 1f) * size.width / 2f
                    val maxOffsetY = (newScale - 1f) * size.height / 2f
                    val newOffsetX = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                    val newOffsetY = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                    offset = Offset(newOffsetX, newOffsetY)
                } else {
                    offset = Offset.Zero
                }
            }
        }
    } else {
        Modifier.pointerInput(file) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.changes.size >= 2) {
                        val zoomChange = event.calculateZoom()
                        if (zoomChange != 1f) {
                            scale = (scale * zoomChange).coerceIn(1f, 5f)
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(transformModifier)
            .pointerInput(file) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (scale > 1.5f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = file,
            contentDescription = "Página de cómic",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                    translationX = if (animatedScale > 1f) offset.x else 0f,
                    translationY = if (animatedScale > 1f) offset.y else 0f
                )
        )
    }
}
