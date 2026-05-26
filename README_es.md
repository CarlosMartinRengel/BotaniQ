# BotaniQ 🪴

**BotaniQ** es una aplicación móvil Android desarrollada como Trabajo de Fin de Grado (TFG) para el ciclo de Desarrollo de Aplicaciones Multiplataforma (DAM). 

Su objetivo es actuar como un asistente proactivo en el cuidado de plantas de interior, combinando gestión de inventario local, diagnóstico mediante Inteligencia Artificial en el borde (*Edge AI*) y recálculo de riegos basado en condiciones meteorológicas, todo ello bajo un enfoque *Offline-First* y respetando la privacidad del usuario.

## Requisitos del Sistema (Entorno de Desarrollo)

Para abrir, compilar y ejecutar este proyecto correctamente, el entorno debe cumplir con los siguientes requisitos:

* **IDE:** Android Studio (versión estable más reciente recomendada).
* **JDK:** Java Development Kit **17** (Estrictamente necesario para la correcta compilación de Gradle).
* **SO Destino:** Android 8.0 (Nivel de API 26) o superior.
* **Hardware de pruebas:** Se recomienda encarecidamente probar la aplicación en un **dispositivo físico** en lugar de un emulador para garantizar el correcto funcionamiento de la librería *CameraX* y el modelo de *TensorFlow Lite*.

## Instrucciones de Instalación y Ejecución

1. **Clonar el repositorio** o descomprimir el código fuente en tu máquina local.
2. Abrir **Android Studio** y seleccionar `File > Open...`, navegando hasta la carpeta raíz del proyecto.
3. Esperar a que Gradle configure el entorno. Si no lo hace automáticamente, forzar la sincronización yendo a `File > Sync Project with Gradle Files`.
4. Una vez finalizada la sincronización sin errores, conectar un dispositivo físico (con depuración USB activada) o iniciar un emulador (API 26+).
5. Seleccionar la configuración `app` y pulsar el botón **Run** (o el atajo `Shift + F10`).
6. *(Opcional)* Para generar el APK, ejecutar en la terminal de Android Studio el comando: `./gradlew assembleDebug`.

## Credenciales de Prueba y Configuraciones

Al ser una aplicación enfocada en la privacidad (*Edge AI*), **BotaniQ no requiere creación de cuentas, inicio de sesión ni contraseñas** para su uso. Toda la información del inventario del usuario se persiste localmente en el dispositivo mediante una base de datos SQLite (Room).

### APIs Externas (OpenWeatherMap)
El módulo de ajuste climático requiere conexión a la API de OpenWeatherMap. 
* Para facilitar la evaluación del proyecto, se ha dejado una API Key genérica inyectada directamente en la clase `WeatherApiService.kt`. 
* *Nota:* En un entorno de producción estricto, esta clave se encontraría ofuscada en el archivo `local.properties` (excluido del control de versiones).

### Permisos del Sistema
Durante el uso de la aplicación, el sistema solicitará en tiempo de ejecución el permiso de **Cámara** (`Manifest.permission.CAMERA`). Este permiso es obligatorio para utilizar los módulos de reconocimiento y diagnóstico de salud. Si se deniega, la app mostrará un aviso de degradación elegante, pero el resto de funciones (inventario y riegos manuales) seguirán operativas.

## Stack Tecnológico y Arquitectura

* **Lenguaje:** Kotlin.
* **Arquitectura:** MVVM (Model-View-ViewModel) recomendada por Google.
* **Interfaz de Usuario:** Jetpack Compose (UI Declarativa) + Material Design 3.
* **Persistencia:** Room Database (SQLite local).
* **Inteligencia Artificial:** TensorFlow Lite (Modelos de redes neuronales ejecutados en local).
* **Red y Asincronía:** Retrofit 2 + OkHttp + Corrutinas de Kotlin (Dispatchers.IO).
* **Tareas en Segundo Plano:** WorkManager (para notificaciones de riego resilientes).