package com.hereliesaz.reup

import android.content.Context
import android.content.Intent
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
 * Now equipped with situational awareness to detect when authorization is granted.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            ReUpTheme(dynamicColor = false) {
                // The root navigator of your despair
                ReUpAppNavigator()
            }
        }
    }
}

@Composable
fun ReUpAppNavigator() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State representing whether the panopticon's eye is legally open
    var isServiceEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    // Observe lifecycle changes. When the user returns from Settings, we re-verify reality.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isServiceEnabled = isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (isServiceEnabled) {
        // The eye is open. Show the configuration of the surveillance state.
        SurveillanceConfigurationScreen(context)
    } else {
        // The eye is blind. Demand authorization.
        ReUpDashboardMain()
    }
}

@Composable
fun ReUpDashboardMain() {
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
                color = EyePupilBlue
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
                text = "STATUS: DORMANT // BLIND",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            Text(
                text = "The digital eye is currently closed. To enforce cognitive surveillance and visual intervention, you must grant accessibility authorization.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { openAccessibilitySettings(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SunRed,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                shape = androidx.compose.foundation.shape.AbsoluteRoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "GRANT PANOPTICON AUTHORIZATION",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Determines if the specific SpiralObserverService has been authorized by the user.
 */
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
        if (componentName.equals(expectedComponentName, ignoreCase = true)) {
            return true
        }
    }
    return false
}

private fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}