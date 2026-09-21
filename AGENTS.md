# AGENTS.md

## Proyecto

App Android **BotaniQ** (Kotlin + Jetpack Compose + Room + TensorFlow Lite). El código vive en `BotaniQ/`. Gestión de inventario de plantas de interior, diagnóstico de salud por Edge AI (offline-first, sin cuentas).

## Backlog: mejoras propuestas (pendientes de revisar)

Ordenado de mayor a menor impacto percibido. Marcar `[x]` lo que se apruebe.

### Imagen y marca
- [x] **Icono de la app**: ya es propio (`botaniq_launcher*`, maceta/hojas, activo en el manifest). Limpieza hecha: borrados los assets placeholder muertos de Android Studio (`mipmap-anydpi/ic_launcher*` y `drawable/ic_launcher_background/foreground.xml`).
- [x] **Splash screen** (Android 12+): logo + color de marca (`values-v31/themes.xml` → `windowSplashScreenBackground` + `windowSplashScreenAnimatedIcon`, tema nativo sin dependencias). En Android <12 no hay splash de sistema.

### Rendimiento (Compose)
- **Baseline Profiles** (macrobenchmark + profileinstaller): si queda jank residual en scroll del grid; remedio estándar de Compose. Requiere módulo nuevo + perfilado en dispositivo.
- **Skeleton loaders** en el grid mientras cargan las fotos (Coil ya permite placeholders).
- Transiciones de tabs ya reducidas a 180ms. Opcional diferenciar: tabs instantáneos (`None`) y detalle/registro con slide para más profundidad.

### Funcionalidad
- **Búsqueda/filtro en inventario**: con la grid de 2 columnas y ~20+ plantas, filtrar por nombre o estado de riego.
- **Widget de escritorio** "hoy toca regar" (reusa los Workers/notificaciones existentes).
- **Compartir planta** (imagen + datos como tarjeta).
- Estado de carga inicial en inventario (hoy puede mostrar vacío un instante si la DB tarda).

### Calidad / técnico
- **Actualizar dependencias**: coreKtx 1.10.1, lifecycle 2.6.1, room 2.6.1, coil 2.6.0, camerax 1.3.3, kotlin 2.1.0, agp 8.7.3 (la BOM de compose ya es reciente, 2026.02.01).
- **Duplicidad material3**: en `app/build.gradle.kts` se declaran `androidx.compose.material3` (BOM) y `androidx.material3` fijada a 1.4.0; basta una.
- **Instrumented UI tests** (Compose testing): hoy solo hay unit tests de lógica; cubrir flujo registrar→detalle→regar→borrar.
- **Strings hardcodeados**: toasts en `CameraScreen` ("Sin permiso de cámara" — ya existe `camera.toast.no_permission` sin usar) y en `RequestNotificationPermission`; pasar a `strings.xml` (ES/EN).
- **Touch targets**: el botón de riego del grid es de 44dp; mínimo recomendado 48dp.
- **Accessibility**: el botón de captura de la cámara no tiene contentDescription.

## Plan aprobado: añadir plantas/enfermedades sin retrain

**Objetivo:** llegar a ~100 especies identificables añadiendo solo datos, sin buscar fotos ni entrenar modelos cada vez.

### Decisión de arquitectura

Embeddings + KNN con extractor **DINOv2 ViT-S/14** congelado (TFLite int8, ~22MB, `model_features.tflite`). Las clases se definen por **datos** (`catalog.json`), no por el modelo. Los labels hardcodeados (`labels.txt`, `labels_disease.txt`) se eliminan.

- **Enfermedades: genéricas por ahora y prescindibles.** El usuario aún decide si elimina toda la parte de enfermedades. Aisladas en `TFLiteAnalyzer.analyzeHealth` + modo DIAGNOSTIC del `ScannerMode`, eliminables sin tocar el reconocimiento de especies.

### Fases

1. **`tools/generate_catalog.py`** (nuevo): entrada = lista de especies; descarga 50–150 fotos/especie de Wikimedia Commons (enfermedades: PlantVillage); filtro de outliers por distancia al centroide; embeddings int8 (384 dims) con el mismo DINOv2; clase negativa ("no planta", ~50–100 embeddings); exporta `catalog.json` versionado + `catalog_seed.json` (~5–8MB con 100 especies).
2. **Extractor on-device**: DINOv2 ViT-S/14 → TFLite int8 en `assets/model_features.tflite`. Nunca se reentrena. Fallback si el export falla: ConvNeXt-Tiny (misma interfaz).
3. **Kotlin — clasificador**: en `data/tensorflow/TFLiteAnalyzer.kt` reemplazar `analyzeSpecies`/`analyzeHealth` por embedding + similitud coseno contra el catálogo; confianza por brecha top-1/top-2 con la clase negativa como suelo de rechazo; fallback de género ("probablemente Monstera spp."). Ajustar `ml/RecognitionResult.kt`/`ml/DiagnosticResult.kt` si hace falta.
4. **Catálogo remoto**: nuevo `data/catalog/` (CatalogEntity Room + CatalogRepository + CatalogFetcher Retrofit). WorkManager refresco diario; caché en Room; fusión con `catalog_seed.json` para primer arranque/offline; comparación por versión.
5. **BD y UI**: `BotaniQDatabase.kt` seed hardcodeado (líneas ~57–121) → carga desde `catalog_seed.json` (misma `SpeciesInfoEntity`, cuidados incluidos; las 9 especies actuales se conservan). `CameraViewModel.kt` adapta umbrales a la confianza calibrada y conserva el registro manual para confianza baja.
6. **Verificación**: assert de que las especies actuales + clases de enfermedad siguen acertando; prueba en dispositivo.

### Workflow: cómo se añade una especie nueva (una vez implementado)

1. Añadir la especie a la lista de entrada del catálogo (`species_list.json` o similar): nombre científico + datos de cuidado (riego, tips).
2. Ejecutar `tools/generate_catalog.py <especie>` → descarga fotos de Wikimedia, filtra outliers, calcula embeddings, regenera `catalog.json` + `catalog_seed.json`.
3. Subir el `catalog.json` al hosting estático. Fin — los usuarios la reciben sin actualizar la app.

El trabajo manual es solo la lista y los cuidados; fotos y "modelo" los hace el script.

### Workflow: validación de la descarga (ya probado)

El 2026-08-09 se probó con **Cinta (Chlorophytum comosum)** descargando 100 miniaturas desde Wikimedia Commons. Lecciones validadas:

- Usar la API `action=query` con `generator=categorymembers` sobre `Category:<Nombre científico>`.
- `prop=imageinfo` con `iiurlwidth=800` devuelve `thumburl`, pero **solo si `iiprop` incluye `url`** (sin `url` no hay thumburl).
- Descargar **miniaturas de 800px**, no la imagen completa: evita el 429 de robot policy (que sí salta con la URL original).
- Rate-limit real: ~1 req/s con sleep de 1s entre descargas y **backoff en 429** (5s/10s/15s). Con eso funciona estable.
- El harness de prueba vive en `/tmp/opencode/fetch_photos.py` (fuera del repo); promocionar a `tools/generate_catalog.py` en la Fase 1.

### Decisiones

- Exactitud real esperada: ~85–92% top-1 en especies distintas; cae en pares parecidos (p.ej. Monstera deliciosa vs borsigiana). No se persigue cero fallos: la confianza calibrada deriva el resto al registro manual (flujo ya existente).
- El % de confianza mostrado se calibra (brecha top-1/top-2), no se muestra la similitud cosena cruda.
- ~100 fotos/especie es el punto dulce para KNN con DINOv2 (500+ solo añade ~1–2pp; la diversidad importa más que la cantidad). Reforzar con más refs (~150) solo las especies confundibles.
- La descarga de Wikimedia va rate-limiteada (~1 req/s, miniatura de 800px, `iiprop=url` necesario para `thumburl`, backoff en 429); 10.000 imágenes ≈ varias horas, una sola vez.
- Hosting del catálogo: estático gratuito (GitHub Pages / Firebase Hosting). Sin coste.
- Nuevas clases llegan a usuarios sin subir versión a Play (solo se actualiza `catalog.json`).

### Prerrequisito detectado (build roto)

`CameraViewModel.kt` referencia estos strings que NO existen en `res/values*/`:

- `ia_mode_background`, `ia_mode_lowConfidence`, `ia_mode_lowConfidence_diagnostic`, `ia_status_healthy`, `ia_status_anomaly`

Añadirlos a `strings.xml` antes de tocar nada más.

### Git / repo

- `AGENTS.md` NO se versiona (decisión del usuario): no añadirlo a git, no commitearlo.
- `BotaniQ/.idea/` está ignorado y des-trackeado (commit `96bafbe`).
