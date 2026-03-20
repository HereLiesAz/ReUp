import java.net.URI
import java.io.FileOutputStream

plugins {
    id("com.android.application")
    id("base")
    id("org.jetbrains.kotlin.plugin.compose")
}

val envVersionName = System.getenv("VERSION_NAME") ?: "0.5.0.0"
val envVersionCode = System.getenv("VERSION_CODE")?.toInt() ?: 1

android {
    namespace = "com.hereliesaz.reup"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hereliesaz.reup"
        minSdk = 26
        targetSdk = 36
        versionCode = envVersionCode
        versionName = envVersionName
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    androidResources {
        noCompress.add("tflite")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildToolsVersion = "36.1.0"
}

base {
    archivesName.set("ReUp-$envVersionName")
}

// The machine fetches its own brain from the void.
tasks.register("downloadCortex") {
    val modelUrl = "https://storage.googleapis.com/download.tensorflow.org/models/tflite/text_classification/text_classification_v2.tflite"
    val destDir = file("src/main/assets")
    val destFile = file("$destDir/sentiment_classifier.tflite")

    doLast {
        if (!destFile.exists()) {
            println("Awaiting neural network geometry...")
            destDir.mkdirs()
            URI(modelUrl).toURL().openStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            println("Cortex successfully grafted into the panopticon.")
        } else {
            println("The machine already possesses a mind. Skipping assimilation.")
        }
    }
}

// Force the brain transplant to occur before the assembly line begins.
tasks.named("preBuild") {
    dependsOn("downloadCortex")
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    
    implementation("com.google.ai.edge.litert:litert:2.1.3")

    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.4") {
        exclude(group = "org.tensorflow", module = "tensorflow-lite")
        exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
    }
    
    implementation("com.google.auto.value:auto-value-annotations:1.11.1")
}
