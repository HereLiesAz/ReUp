# ReUp

ReUp is an Android accessibility tool designed to assist with cognitive behavioral tracking and emotional regulation. By monitoring text input across the device in real-time, the application identifies negative sentiments or cognitive distortions and provides immediate visual feedback, allowing users to recognize and interrupt downward psychological spirals.

## Features

* **Real-Time On-Device ML Inference:** Utilizes an `AccessibilityService` alongside a pre-trained LiteRT (TensorFlow Lite) natural language classifier to read text fields across all applications and mathematically evaluate the probability of despair in real-time.
* **Adjustable Paranoia:** A Jetpack Compose UI allows the user to dynamically adjust the neural network's sensitivity threshold, dictating exactly how much negativity is permitted before the machine intervenes.
* **Visual Feedback:** Employs a `SYSTEM_ALERT_WINDOW` overlay to highlight detected negative text with a translucent red hemorrhage, physically drawing the user's attention to their own cognitive distortions.
* **Data Logging:** Silently records the exact phrasing and timestamps of detected despair into a local SQLite database, utilizing coroutines to ensure the void is documented without choking the main thread.
* **Analytics Visualization:** Renders logged data as a jagged line chart against the relentless progression of time.

## Architecture & Technical Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Background Processing:** Kotlin Coroutines (`Dispatchers.IO`)
* **Local Storage:** SQLite (`SQLiteOpenHelper`)
* **Machine Learning:** LiteRT / TensorFlow Lite Task Library (`NLClassifier`)
* **Core APIs:** * `AccessibilityService` (for voyeurism and semantic coordinate retrieval via `EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY`)
    * `WindowManager` (for projecting the `HighlightView` overlay)
* **CI/CD:** GitHub Actions (Automated $a.b.c.d$ vectoring and debug APK generation)

## Permissions Required

The operating system considers this application fundamentally hostile. It requires explicit authorization for:

1.  **Accessibility Service:** To observe the text being typed in active windows and interrogate the host application for screen coordinates.
2.  **Display over other apps (`SYSTEM_ALERT_WINDOW`):** To draw the intervention overlay on top of the host application.

## Installation & Build

This project utilizes a GitHub Actions workflow for automated builds. If building locally, the Gradle daemon is scripted to automatically download the necessary LiteRT neural network from TensorFlow Hub during the `preBuild` phase.

1.  Clone the void.
2.  Ensure you have a `version.properties` file in the root directory:
    ~~~properties
    MAJOR=1
    MINOR=0
    ~~~
3.  Command the assembly line:
    ~~~bash
    ./gradlew assembleDebug
    ~~~

## Privacy Note

ReUp processes all text and neural network inference locally on the device. The machine is isolated. The accessibility service does not transmit your breakdowns to external servers. The SQLite database is securely sandboxed.
