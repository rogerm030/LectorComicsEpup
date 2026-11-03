package com.example.lectorcomicsylibros.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lectorcomicsylibros.data.settings.PresetColor
import com.example.lectorcomicsylibros.ui.theme.colorIntToHsv
import com.example.lectorcomicsylibros.ui.theme.hsvToColorInt

@Composable
fun CustomColorPickerDialog(
    initialColorHex: Int,
    onDismissRequest: () -> Unit,
    onColorSelected: (Int) -> Unit
) {
    var currentColorInt by remember { mutableIntStateOf(initialColorHex) }

    // Convert current color to RGB & HSV
    val r = (currentColorInt shr 16) and 0xFF
    val g = (currentColorInt shr 8) and 0xFF
    val b = currentColorInt and 0xFF

    val hsv = remember(currentColorInt) {
        val array = FloatArray(3)
        colorIntToHsv(currentColorInt, array)
        array
    }

    var hue by remember(currentColorInt) { mutableFloatStateOf(hsv[0]) }
    var saturation by remember(currentColorInt) { mutableFloatStateOf(hsv[1]) }
    var value by remember(currentColorInt) { mutableFloatStateOf(hsv[2]) }

    fun updateFromHsv(h: Float, s: Float, v: Float) {
        hue = h
        saturation = s
        value = v
        val colorInt = hsvToColorInt(floatArrayOf(h, s, v))
        currentColorInt = colorInt
    }

    fun updateFromRgb(red: Int, green: Int, blue: Int) {
        val colorInt = (0xFF shl 24) or ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or (blue and 0xFF)
        currentColorInt = colorInt
        val array = FloatArray(3)
        colorIntToHsv(colorInt, array)
        hue = array[0]
        saturation = array[1]
        value = array[2]
    }

    val hexString = String.format("#%06X", 0xFFFFFF and currentColorInt)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Selector de Color Personalizado",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(currentColorInt))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = hexString,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Preset Swatches
                Text(
                    text = "Colores Predeterminados",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        PresetColor.PURPLE,
                        PresetColor.TEAL,
                        PresetColor.BLUE,
                        PresetColor.GREEN,
                        PresetColor.ORANGE,
                        PresetColor.RED
                    ).forEach { preset ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(preset.colorHex))
                                .border(
                                    width = if (currentColorInt == preset.colorHex) 3.dp else 1.dp,
                                    color = if (currentColorInt == preset.colorHex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    currentColorInt = preset.colorHex
                                    val array = FloatArray(3)
                                    colorIntToHsv(preset.colorHex, array)
                                    hue = array[0]
                                    saturation = array[1]
                                    value = array[2]
                                }
                        )
                    }
                }

                HorizontalDivider()

                // HSV Sliders
                Text(
                    text = "Ajuste HSV (Tono, Saturación, Valor)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                // Hue (0 .. 360)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Matiz (Hue)", style = MaterialTheme.typography.bodySmall)
                        Text("${hue.toInt()}°", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = hue,
                        onValueChange = { updateFromHsv(it, saturation, value) },
                        valueRange = 0f..360f
                    )
                }

                // Saturation (0 .. 1)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Saturación", style = MaterialTheme.typography.bodySmall)
                        Text("${(saturation * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = saturation,
                        onValueChange = { updateFromHsv(hue, it, value) },
                        valueRange = 0f..1f
                    )
                }

                // Value / Brightness (0 .. 1)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Brillo (Value)", style = MaterialTheme.typography.bodySmall)
                        Text("${(value * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = value,
                        onValueChange = { updateFromHsv(hue, saturation, it) },
                        valueRange = 0f..1f
                    )
                }

                HorizontalDivider()

                // RGB Sliders
                Text(
                    text = "Ajuste RGB (Rojo, Verde, Azul)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                // Red (0 .. 255)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rojo (Red)", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                        Text("$r", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = r.toFloat(),
                        onValueChange = { updateFromRgb(it.toInt(), g, b) },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                    )
                }

                // Green (0 .. 255)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Verde (Green)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                        Text("$g", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = g.toFloat(),
                        onValueChange = { updateFromRgb(r, it.toInt(), b) },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF2E7D32), activeTrackColor = Color(0xFF2E7D32))
                    )
                }

                // Blue (0 .. 255)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Azul (Blue)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
                        Text("$b", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = b.toFloat(),
                        onValueChange = { updateFromRgb(r, g, it.toInt()) },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF1565C0), activeTrackColor = Color(0xFF1565C0))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(currentColorInt)
                    onDismissRequest()
                }
            ) {
                Text("Aplicar Color")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
