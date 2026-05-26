# BotaniQ 🪴

[Leer en Español](README_es.md)

**BotaniQ** is an Android mobile application developed as a Final Degree Project (TFG) for the Cross-Platform Application Development (DAM) program.

Its goal is to act as a proactive indoor plant care assistant, combining local inventory management, health diagnosis via *Edge AI*, and watering recalculations based on weather conditions. All of this is built with an *Offline-First* approach, prioritizing user privacy.

## System Requirements (Development Environment)

To open, compile, and run this project successfully, your environment must meet the following requirements:

* **IDE:** Android Studio (latest stable version recommended).
* **JDK:** Java Development Kit **17** (Strictly required for proper Gradle compilation).
* **Target OS:** Android 8.0 (API Level 26) or higher.
* **Testing Hardware:** It is highly recommended to test the application on a **physical device** rather than an emulator to ensure the proper functioning of the *CameraX* library and the *TensorFlow Lite* model.

## Installation and Execution Instructions

1. **Clone the repository** or unzip the source code on your local machine.
2. Open **Android Studio** and select `File > Open...`, navigating to the project's root folder.
3. Wait for Gradle to configure the environment. If it doesn't start automatically, force the synchronization by going to `File > Sync Project with Gradle Files`.
4. Create a `local.properties` file in the root of the project (if it doesn't exist) and add your OpenWeatherMap API key like this: `OPEN_WEATHER_API_KEY="your_api_key_here"`.
5. Once synchronization finishes without errors, connect a physical device (with USB debugging enabled) or start an emulator (API 26+).
6. Select the `app` configuration and click the **Run** button (or use the shortcut `Shift + F10`).
7. *(Optional)* To generate the APK, run the following command in the Android Studio terminal: `./gradlew assembleDebug`.

## Test Credentials and Configuration

Since this is a privacy-focused application (*Edge AI*), **BotaniQ does not require account creation, logins, or passwords** to be used. All user inventory data is persisted locally on the device using a SQLite database (Room).

### External APIs (OpenWeatherMap)
The weather adjustment module requires a connection to the OpenWeatherMap API.
* For security reasons, the API key is not included in the repository. As mentioned in the installation steps, you must provide your own key in the `local.properties` file.

### System Permissions
During the use of the application, the system will request the **Camera** permission (`Manifest.permission.CAMERA`) at runtime. This permission is mandatory to use the plant recognition and health diagnosis modules. If denied, the app will show a graceful degradation warning, but all other features (inventory and manual watering) will remain fully operational.

## Tech Stack and Architecture

* **Language:** Kotlin.
* **Architecture:** MVVM (Model-View-ViewModel) as recommended by Google.
* **User Interface:** Jetpack Compose (Declarative UI) + Material Design 3.
* **Persistence:** Room Database (Local SQLite).
* **Artificial Intelligence:** TensorFlow Lite (Neural network models executed locally).
* **Network & Asynchrony:** Retrofit 2 + OkHttp + Kotlin Coroutines (Dispatchers.IO).
* **Background Tasks:** WorkManager (for resilient watering notifications).