// app/src/main/kotlin/com/hereliesaz/reup/ConfigurationUi.kt

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hereliesaz.reup.ui.theme.*
import com.hereliesaz.reup.SpiralConfig as Config
import java.io.File

/**
 * The command center for your surveillance.
 * The faulty Paranoia Slider has been excised.
 * Text file I/O integrated for Custom Triggers.
 */
@Composable
fun SurveillanceConfigurationScreen(context: Context) {
    val prefs = remember { context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE) }
    val dbHelper = remember { SpiralDatabaseHelper.getInstance(context) }

    var currentMask by remember { mutableIntStateOf(prefs.getInt(Config.KEY_FILTER_MASK, Config.DEFAULT_MASK)) }
    var dailyStats by remember { mutableStateOf(emptyList<DailyDistortion>()) }

    // --- NEW: Text File Integration ---
    val triggersFile = remember { File(context.filesDir, Config.FILE_CUSTOM_PHRASES) }

    var customPhrases by remember {
        mutableStateOf(
            if (triggersFile.exists()) triggersFile.readLines().filter { it.isNotBlank() }
            else emptyList()
        )
    }

    var newPhrase by remember { mutableStateOf("") }
    var testText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dailyStats = dbHelper.getDailyCounts()
    }

    val scrollState = rememberScrollState()

    fun updateMask(flag: Int) {
        currentMask = Config.toggleFlag(currentMask, flag)
        prefs.edit().putInt(Config.KEY_FILTER_MASK, currentMask).apply()
    }

    // --- NEW: Writes directly to the .txt file on the device ---
    fun saveCustomPhrases(phrases: List<String>) {
        customPhrases = phrases
        triggersFile.writeText(phrases.joinToString("\n"))
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

        SectionHeader("THE JAGGED VOID (Despair Analytics)")
        JaggedLineChart(dailyStats)

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        SectionHeader("CUSTOM TRIGGERS")
        Text(
            "Saved to: ${triggersFile.absolutePath}",
            style = MaterialTheme.typography.labelSmall,
            color = EyePupilBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Add specific phrases or words for the machine to monitor alongside the neural network.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            OutlinedTextField(
                value = newPhrase,
                onValueChange = { newPhrase = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = Color.White),
                placeholder = { Text("e.g., I can't do this", color = Color.White.copy(alpha = 0.4f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RayGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = RayGold
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newPhrase.isNotBlank() && !customPhrases.contains(newPhrase.trim())) {
                        saveCustomPhrases(customPhrases + newPhrase.trim())
                        newPhrase = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EyePupilBlue),
                modifier = Modifier.height(56.dp)
            ) {
                Text("ADD", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        customPhrases.forEach { phrase ->
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.2f)).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(phrase, color = RayGold, style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = { saveCustomPhrases(customPhrases - phrase) }) {
                    Text("REMOVE", color = SunRed, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
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

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        SectionHeader("CALIBRATION TERMINAL")
        Text(
            "Test the machine's neural thresholds and custom triggers safely within the app.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = testText,
            onValueChange = { testText = it },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            textStyle = TextStyle(color = Color.White),
            placeholder = { Text("Try some negativity! Just to see how it works...", color = Color.White.copy(alpha = 0.4f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RayGold,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                cursorColor = RayGold
            )
        )

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