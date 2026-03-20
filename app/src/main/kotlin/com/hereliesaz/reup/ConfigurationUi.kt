// hereliesaz/reup/ReUp-9db2805a9ede9350d55e55d72acf9c1535bb70f4/app/src/main/kotlin/com/hereliesaz/reup/ConfigurationUi.kt

package com.hereliesaz.reup

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hereliesaz.reup.ui.theme.*
import com.hereliesaz.reup.SpiralConfig as Config

/**
 * The command center for your surveillance.
 * Now includes Adjustable Paranoia and Daily Analytics.
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

    val checkColors = CheckboxDefaults.colors(
        checkedColor = EyePupilBlue,
        uncheckedColor = Color.White.copy(alpha = 0.6f)
    )

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

        // --- ADJUSTABLE PARANOIA ---
        SectionHeader("ADJUSTABLE PARANOIA (Sensitivity)")
        Text(
            "Dictates the neural network's threshold for detecting despair ($sensitivity).",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
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

        // --- FOCUS SECTION ---
        SectionHeader("FOCUS (Toward What)")
        ConfigCheckbox("SELF", Config.isEnabled(currentMask, Config.FOCUS_SELF), checkColors) { updateMask(Config.FOCUS_SELF) }
        ConfigCheckbox("OTHERS", Config.isEnabled(currentMask, Config.FOCUS_OTHERS), checkColors) { updateMask(Config.FOCUS_OTHERS) }
        ConfigCheckbox("WORLD", Config.isEnabled(currentMask, Config.FOCUS_WORLD), checkColors) { updateMask(Config.FOCUS_WORLD) }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // --- LEDGER ANALYTICS ---
        SectionHeader("THE LEDGER (Daily Despair)")
        if (dailyStats.isEmpty()) {
            Text("No cognitive collapses recorded. Yet.", color = Color.White.copy(alpha = 0.4f))
        } else {
            dailyStats.forEach { stat ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stat.dateLabel, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Text("${stat.count} events", color = RayGold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = RayGold, modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
fun ConfigCheckbox(label: String, checked: Boolean, colors: CheckboxColors, onCheckedChange: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { _ -> onCheckedChange() }, colors = colors)
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.padding(start = 8.dp))
    }
}
