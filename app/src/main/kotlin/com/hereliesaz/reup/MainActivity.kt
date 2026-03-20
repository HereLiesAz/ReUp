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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // We do not care about the result code. We merely refresh the UI state.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
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
                        .background(Color.Black),
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
                            text = "The void is currently empty.\nEnable the Accessibility Service in OS Settings to begin.",
                            color = Color.DarkGray,
                            fontFamily = FontFamily.Monospace,
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
                                text = "GEOMETRY OF DESPAIR",
                                color = Color.DarkGray,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 24.dp)
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
        // Force UI to recompose if they magically enabled the permission in the background.
        setContent { /* Re-evaluate state via standard Compose mechanics */ }
    }
}

@Composable
fun PermissionPrompt(onRequestPermission: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "AUTHORIZATION REQUIRED",
            color = Color(0xFF8B0000),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "To project hallucinations onto other applications, you must grant the 'Display over other apps' permission.",
            color = Color.LightGray,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 24.dp, start = 32.dp, end = 32.dp)
        )
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("GRANT ACCESS", color = Color.Black, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun DespairChart(data: List<DailyDistortion>) {
    val maxCount = remember(data) { data.maxOfOrNull { it.count }?.toFloat()?.takeIf { it > 0 } ?: 1f }
    val lineColor = Color(0xFF8B0000) 

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
                color = Color.DarkGray,
                radius = 6f,
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 8f)
        )
    }
}
