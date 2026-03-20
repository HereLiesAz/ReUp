# ReUp

Sometimes all it takes to break a habit is a gentle reminder. Sometimes it takes more, sure, but it's at least a starting point. When it comes to pessimism, undue negativity, or subtle acts of self-deprication, you usually don't even notice that that's what's happening. And if it's a human being reminding you, it can feel like a personal assault. ReUp is a non-human reminder, and that's all. It won't stop you from being negative. It's only job is to gently let you know about the negative patterns in your speech, in case you didn't. The rest is up to you. (And you can do it! Yay! Go team!)

## Features

* **Real-Time On-Device ML Inference:** Utilizes an `AccessibilityService` alongside a LiteRT (TensorFlow Lite) `NLClassifier` to mathematically evaluate the probability of despair in real-time.
* **Adjustable Paranoia:** A Jetpack Compose UI allows the user to dynamically adjust the neural network's sensitivity threshold ($P_{despair}$), dictating exactly how much negativity is permitted before the machine intervenes.
* **Visual Feedback:** Employs a `SYSTEM_ALERT_WINDOW` overlay to highlight detected negative text with a translucent red hemorrhage, physically drawing the user's attention to their own cognitive distortions.
* **Data Logging:** Silently records the exact phrasing and timestamps of detected despair into a local SQLite database, utilizing coroutines to ensure the void is documented without choking the main thread.
* **Analytics Visualization:** Renders logged data as a jagged line chart against the relentless progression of time.

## Architecture & Technical Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Background Processing:** Kotlin Coroutines (`Dispatchers.IO`)
* **Local Storage:** SQLite (`SQLiteOpenHelper`)
* **Machine Learning:** LiteRT / TensorFlow Lite Task Library (`NLClassifier`)
* **Core APIs:** * `AccessibilityService` (for surveillance and semantic coordinate retrieval via `EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY`)
    * `WindowManager` (for projecting the `HighlightView` overlay via `SYSTEM_ALERT_WINDOW`)
* **CI/CD:** GitHub Actions (Automated $a.b.c.d$ vectoring and debug APK generation)

## Permissions Required

The operating system considers this application fundamentally hostile. It requires explicit authorization for:

1.  **Accessibility Service:** To observe the text being typed in active windows and interrogate the host application for screen coordinates.
2.  **Display over other apps (`SYSTEM_ALERT_WINDOW`):** To draw the intervention overlay on top of the host application.


## Privacy Note

ReUp processes all text and neural network inference locally on the device. The machine is isolated. The accessibility service does not transmit your breakdowns to external servers. The SQLite database is securely sandboxed.
