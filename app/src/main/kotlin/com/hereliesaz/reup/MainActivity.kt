// hereliesaz/reup/ReUp-9db2805a9ede9350d55e55d72acf9c1535bb70f4/app/src/main/kotlin/com/hereliesaz/reup/MainActivity.kt

package com.hereliesaz.reup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hereliesaz.reup.ui.theme.*

/**
 * The primary anchor point for the digital panopticon.
 * Now equipped with situational awareness to detect when overlay and accessibility authorization is granted.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            ReUpTheme(dynamicColor = false) {
                ReUpAppNavigator()
            }
        }
    }
}

@Composable
fun ReUpAppNavigator() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isAccessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var isOverlayEnabled by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
                isOverlayEnabled = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (isAccessibilityEnabled && isOverlayEnabled) {
        SurveillanceConfigurationScreen(context)
    } else {
        ReUpDashboardMain(isAccessibilityEnabled, isOverlayEnabled)
    }
}

@Composable
fun ReUpDashboardMain(isAccessibilityEnabled: Boolean, isOverlayEnabled: Boolean) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyIndigo)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = if (isAccessibilityEnabled && isOverlayEnabled) EyePupilBlue else SunRed
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .background(color = Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "REUP PANOPTICON",
                style = MaterialTheme.typography.displaySmall,
                color = RayGold,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "STATUS: ${if (!isAccessibilityEnabled || !isOverlayEnabled) "DORMANT // BLIND" else "ACTIVE"}",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            Text(
                text = "The digital eye is currently obstructed. To enforce cognitive surveillance and visual intervention, you must grant the following authorizations.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (!isAccessibilityEnabled) {
                PermissionButton("GRANT ACCESSIBILITY ACCESS") {
                    openAccessibilitySettings(context)
                }
            }

            if (!isOverlayEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                PermissionButton("GRANT OVERLAY PERMISSION") {
                    openOverlaySettings(context)
                }
            }
        }
    }
}

@Composable
fun PermissionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = SunRed,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        shape = androidx.compose.foundation.shape.AbsoluteRoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = "${context.packageName}/${SpiralObserverService::class.java.canonicalName}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)

    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedComponentName, ignoreCase = true)) return true
    }
    return false
}

private fun openAccessibilitySettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

private fun openOverlaySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    context.startActivity(intent)
}
