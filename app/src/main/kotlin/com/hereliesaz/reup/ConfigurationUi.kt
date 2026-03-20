// hereliesaz/reup/ReUp-9db2805a9ede9350d55e55d72acf9c1535bb70f4/app/src/main/kotlin/com/hereliesaz/reup/ConfigurationUi.kt

package com.hereliesaz.reup

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.hereliesaz.reup.ui.theme.*
import com.hereliesaz.reup.SpiralConfig as Config

/**
 * The command center for your surveillance.
 * The illusion of choice has been corrected.
 */
@Composable
fun SurveillanceConfigurationScreen(context: Context) {
    val prefs = remember { context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE) }
    val dbHelper = remember { SpiralDatabaseHelper(context) }
    
    var currentMask by remember { mutableIntStateOf(prefs.getInt(Config.KEY_FILTER_MASK, Config.DEFAULT_MASK)) }
    var sensitivity by remember { mutableFloatStateOf(prefs.getFloat(Config.KEY_SENSITIVITY, Config.DEFAULT_SENSITIVITY)) }
    var dailyStats by remember { mutableStateOf(emptyList<DailyDistortion>()) }

    LaunchedEffect(Unit) {
        dailyStats = dbHelper.getDailyCounts()
    }

    val scrollState = rememberScrollState()

    fun updateMask(flag: Int) {
        currentMask = Config.toggleFlag(currentMask, flag)
        prefs.edit().putInt(Config.KEY_FILTER_MASK, currentMask).apply()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyIndigo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Text("CONFIGURATION", style = MaterialTheme.typography.headlineMedium, color = RayGold)
        
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader("ADJUSTABLE PARANOIA")
        Text(
            "Neural sensitivity threshold ($sensitivity). Lower values invite more interference.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        Slider(
            value = sensitivity,
            onValueChange = { 
                sensitivity = it
                prefs.edit().putFloat(Config.KEY_SENSITIVITY, it).apply()
            },
            colors = SliderDefaults.colors(thumbColor = SunRed, activeTrackColor = RayGold)
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        SectionHeader("THE JAGGED VOID (Despair Analytics)")
        JaggedLineChart(dailyStats)

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        SectionHeader("FOCUS VECTORS")
        val checkColors = CheckboxDefaults.colors(checkedColor = EyePupilBlue, uncheckedColor = Color.White.copy(alpha = 0.5f))
        ConfigCheckbox("SELF", Config.isEnabled(currentMask, Config.FOCUS_SELF), checkColors) { updateMask(Config.FOCUS_SELF) }
        ConfigCheckbox("OTHERS", Config.isEnabled(currentMask, Config.FOCUS_OTHERS), checkColors) { updateMask(Config.FOCUS_OTHERS) }
        ConfigCheckbox("WORLD", Config.isEnabled(currentMask, Config.FOCUS_WORLD), checkColors) { updateMask(Config.FOCUS_WORLD) }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        SectionHeader("TYPE VECTORS")
        ConfigCheckbox("DESPAIR", Config.isEnabled(currentMask, Config.TYPE_DESPAIR), checkColors) { updateMask(Config.TYPE_DESPAIR) }
        ConfigCheckbox("WORTHLESS", Config.isEnabled(currentMask, Config.TYPE_WORTHLESS), checkColors) { updateMask(Config.TYPE_WORTHLESS) }
        ConfigCheckbox("ANGER", Config.isEnabled(currentMask, Config.TYPE_ANGER), checkColors) { updateMask(Config.TYPE_ANGER) }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun JaggedLineChart(data: List<DailyDistortion>) {
    val counts = data.map { it.count.toFloat() }
    val maxCount = (counts.maxOrNull() ?: 1f).coerceAtLeast(5f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (counts.size < 2) {
                drawContext.canvas.nativeCanvas.drawText("Awaiting sufficient despair...", 40f, 80f, android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 40f
                })
                return@Canvas
            }

            val width = size.width
            val height = size.height
            val spacing = width / (counts.size - 1)
            
            val path = Path().apply {
                counts.forEachIndexed { index, count ->
                    val x = index * spacing
                    val y = height - (count / maxCount * height)
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }

            drawPath(path = path, color = RayGold, style = Stroke(width = 4f))
            
            counts.forEachIndexed { index, count ->
                val x = index * spacing
                val y = height - (count / maxCount * height)
                drawCircle(color = SunRed, radius = 6f, center = Offset(x, y))
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = RayGold, modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
fun ConfigCheckbox(label: String, checked: Boolean, colors: CheckboxColors, onCheckedChange: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { _ -> onCheckedChange() }, colors = colors)
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.padding(start = 8.dp))
    }
}
