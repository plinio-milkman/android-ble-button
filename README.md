# Android BLE Button

An Android sample application to connect to a BLE (Bluetooth Low Energy) device, such as a smart button, and react to its events. This project serves as a practical exploration of modern Android APIs for managing BLE connections, background services, and notifications.

## Key Features

*   **BLE Connection:** Connects to a specified BLE device using its MAC address.
*   **Background Service:** Uses a `ForegroundService` to maintain a stable and active BLE connection even when the app is not in the foreground.
*   **Event Handling:** Listens for notifications from a specific BLE characteristic to detect events, such as a button press.
*   **User Feedback:** Provides immediate feedback upon a button press through:
    *   A high-priority visual notification.
    *   A notification sound, robustly handled to ensure playback across different devices and OEM customizations.
*   **Simple UI:** A minimal user interface to input the device address and manage the connection.

## Project Structure

The project is structured following **Clean Architecture** principles to separate concerns, improve testability, and enhance maintainability. The main layers are:

*   **`presentation`**: Contains the UI (Jetpack Compose), `ViewModel`, Android `Service`, and framework-dependent utilities (notification handling, audio playback, permissions).
*   **`domain`**: The core of the application. It holds the pure business logic (use cases like `HandleButtonPressedUseCase`) and framework-agnostic data models. It is a pure Kotlin module.
*   **`data`**: Implements the repositories defined in the domain layer, managing concrete data sources like the Android BLE APIs (`AndroidBleDataSource`) and `SharedPreferences` (`AndroidPreferenceDataSource`).

## Lessons Learned & Solutions

Several challenges were encountered during development, particularly related to Android's restrictions and OEM-specific customizations. The solutions adopted represent best practices for similar applications:

1.  **Connection Stability:** Using a `ForegroundService` with `foregroundServiceType="connectedDevice"` proved essential to prevent the system from killing the BLE connection when the app is in the background.
2.  **Notification Sound Reliability:** Many devices ignore the sound of notifications sent from a background service, even with `IMPORTANCE_HIGH`. The solution was a hybrid approach:
    *   The **visual notification** is displayed using `NotificationManager`.
    *   The **sound** is played in parallel and manually using `MediaPlayer`, ensuring the user always receives audio feedback.
3.  **Permission Handling:** The app correctly implements runtime permission requests for modern Android versions, including `POST_NOTIFICATIONS` (Android 13+), `BLUETOOTH_SCAN`, and `BLUETOOTH_CONNECT` (Android 12+).

## Getting Started

1.  Clone the repository:
    bash git clone https://github.com/your- username/ android- ble- button. git
2.  Open the project in a recent version of Android Studio.
3.  Build and run the app on a physical device with Bluetooth enabled.
4.  Enter the MAC address of your BLE device in the app and start the connection.

## Key Dependencies

*   [Jetpack Compose](https://developer.android.com/jetpack/compose) for the user interface.
*   [Kotlin Coroutines & Flow](https://developer.android.com/kotlin/coroutines) for managing concurrency and asynchronous data streams.
*   [AndroidX Activity & ViewModel](https://developer.android.com/topic/libraries/architecture) for UI lifecycle management.
*   [AndroidX Preference](https://developer.android.com/guide/topics/ui/settings/use-saved-values) for saving user preferences.