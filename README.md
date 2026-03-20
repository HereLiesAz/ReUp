# ReUp

ReUp is an Android accessibility tool designed to assist with cognitive behavioral tracking and emotional regulation. By monitoring text input across the device in real-time, the application identifies predetermined negative sentiments or cognitive distortions and provides immediate visual feedback, allowing users to recognize and interrupt downward psychological spirals.

## Features

* **Real-Time Intervention:** Utilizes an `AccessibilityService` to monitor text fields across all applications for specific, predefined phrases indicative of negative cognitive patterns.
* **Visual Feedback:** Employs a `SYSTEM_ALERT_WINDOW` overlay to highlight detected negative text with a translucent red indicator, drawing the user's immediate attention to their phrasing.
* **Data Logging:** Silently records the frequency and timestamps of detected phrases into a local SQLite database, utilizing coroutines to ensure background processing does not interrupt the main UI thread or accessibility event pipeline.
* **Analytics Visualization:** Provides a Jetpack Compose dashboard that renders logged data as a line chart, allowing users to track the frequency of their cognitive distortions over time.

## Architecture & Technical Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Background Processing:** Kotlin Coroutines (`Dispatchers.IO`)
* **Local Storage:** SQLite (`SQLiteOpenHelper`)
* **Core APIs:** * `AccessibilityService` (for text interception and coordinate retrieval via `EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY`)
    * `WindowManager` (for drawing the `HighlightView` overlay)
* **CI/CD:** GitHub Actions (Automated versioning and debug APK release generation)

## Permissions Required

Due to the nature of the application's functionality, it requires two sensitive Android system permissions:

1.  **Accessibility Service:** Required to read the text being typed in active windows and to retrieve the bounding box coordinates of specific characters on the screen.
2.  **Display over other apps (`SYSTEM_ALERT_WINDOW`):** Required to draw the visual highlight overlay on top of the host application where the text is being typed.

## Installation & Build

This project utilizes a GitHub Actions workflow for automated builds. To build the project locally:

1.  Clone the repository.
2.  Ensure you have a `version.properties` file in the root directory:
    ~~~properties
    MAJOR=1
    MINOR=0
    ~~~
3.  Build the debug APK using the Gradle wrapper:
    ~~~bash
    ./gradlew assembleDebug
    ~~~

## Privacy Note

ReUp processes all text locally on the device. The accessibility service does not transmit keystrokes or logged data to any external servers. The SQLite database is private to the application's sandboxed storage.
