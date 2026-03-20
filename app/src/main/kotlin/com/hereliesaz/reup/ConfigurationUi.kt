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
 * Includes a testing void to verify the machine's reactive geometry.
 */
@Composable
fun SurveillanceConfigurationScreen(context: Context) {
    val prefs = remember { context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE) }
    var currentMask by remember { mutableIntStateOf(prefs.getInt(Config.KEY_FILTER_MASK, Config.DEFAULT_MASK)) }
    var testInput by remember { mutableStateOf("") }

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
        Text(
            "CONFIGURATION",
            style = MaterialTheme.typography.headlineMedium,
            color = RayGold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "The panopticon only intervenes when the selected vectors align with detected text.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // --- FOCUS SECTION ---
        SectionHeader("FOCUS (Toward What)")
        ConfigCheckbox("SELF (Internalized)", Config.isEnabled(currentMask, Config.FOCUS_SELF), checkColors) {
            updateMask(Config.FOCUS_SELF)
        }
        ConfigCheckbox("OTHERS (Externalized)", Config.isEnabled(currentMask, Config.FOCUS_OTHERS), checkColors) {
            updateMask(Config.FOCUS_OTHERS)
        }
        ConfigCheckbox("WORLD (Systemic)", Config.isEnabled(currentMask, Config.FOCUS_WORLD), checkColors) {
            updateMask(Config.FOCUS_WORLD)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TYPE SECTION ---
        SectionHeader("TYPE (Flavor)")
        ConfigCheckbox("DESPAIR (Hopelessness)", Config.isEnabled(currentMask, Config.TYPE_DESPAIR), checkColors) {
            updateMask(Config.TYPE_DESPAIR)
        }
        ConfigCheckbox("WORTHLESSNESS (Internal Critique)", Config.isEnabled(currentMask, Config.TYPE_WORTHLESS), checkColors) {
            updateMask(Config.TYPE_WORTHLESS)
        }
        ConfigCheckbox("ANGER/BLAME (Hostility)", Config.isEnabled(currentMask, Config.TYPE_ANGER), checkColors) {
            updateMask(Config.TYPE_ANGER)
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // --- THE TESTING VOID ---
        SectionHeader("INTERVENTION TEST CHAMBER")
        Text(
            "Type triggers like 'pointless' (Mild), 'worthless' (Moderate), or 'failure' (Visceral) to verify visual interference.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = testInput,
            onValueChange = { testInput = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Speak into the abyss...", color = Color.White.copy(alpha = 0.3f)) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
                cursorColor = RayGold,
                focusedIndicatorColor = EyePupilBlue,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(64.dp)) // Clearance for navigation
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = RayGold,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun ConfigCheckbox(label: String, checked: Boolean, colors: CheckboxColors, onCheckedChange: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { _ -> onCheckedChange() }, colors = colors)
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}