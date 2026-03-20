package com.hereliesaz.reup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hereliesaz.reup.ui.theme.ReUpTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Refresh UI state
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReUpTheme {
                var data by remember { mutableStateOf<List<DailyDistortion>>(emptyList()) }
                var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(this)) }

                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val dbHelper = SpiralDatabaseHelper(this@MainActivity)
                        data = dbHelper.getDailyCounts()
                        dbHelper.close()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    if (!hasOverlayPermission) {
                        PermissionPrompt {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                            overlayPermissionLauncher.launch(intent)
                            hasOverlayPermission = Settings.canDrawOverlays(this@MainActivity)
                        }
                    } else if (data.isEmpty()) {
                        Text(
                            text = "THE VOID IS EMPTY.",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ENTROPY",
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 48.dp)
                            )
                            DespairChart(data = data)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setContent { /* Recompose */ }
    }
}

@Composable
fun PermissionPrompt(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AUTHORIZATION",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "SYSTEM_ALERT_WINDOW required for cognitive interference.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "GRANT ACCESS", 
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun DespairChart(data: List<DailyDistortion>) {
    val maxCount = remember(data) { data.maxOfOrNull { it.count }?.toFloat()?.takeIf { it > 0 } ?: 1f }
    val lineColor = MaterialTheme.colorScheme.primary
    val nodeColor = MaterialTheme.colorScheme.onBackground

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / if (data.size > 1) (data.size - 1) else 1f

        val path = Path()

        data.forEachIndexed { index, dailyDistortion ->
            val x = index * stepX
            val y = height - ((dailyDistortion.count / maxCount) * height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            drawCircle(
                color = nodeColor,
                radius = 8f,
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 12f)
        )
    }
}
