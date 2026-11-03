# Project Plan

Una aplicación Android (APK) para leer cómics (CBR, CBZ y otros formatos de cómics) y libros en formato EPUB.
Características principales:
- Agregar archivos locales y desde la nube.
- Crear y gestionar categorías/colecciones.
- Opciones de lectura para cómics: Zoom, Pan, Vista en modo Galería / Mosaico de páginas, ajuste de página.
- Opciones de lectura para EPUB: Cambio de tipo de letra (fuente), tamaño de letra, interlineado, márgenes.
- Modos y colores de lectura: Oscuro, Gris, Blanco, Sepia.
- Control de brillo (subir y bajar brillo dentro de la app).
- Modo Claro y Oscuro global.
- Personalización de color de la aplicación con paleta de colores predeterminados y un selector de color totalmente personalizado (Custom Color Picker).
- Conexión e integración con Google Drive y OneDrive para importar/leer archivos.
- Interfaz de usuario adaptada a Material UI / Material Design 3 con Jetpack Compose.

## Project Brief

# Project Brief: Lector de Cómics y Libros (MVP)

## Features

1. **Lector Multiformato (Cómics y EPUB)**
   - Visor interactivo para cómics (CBR, CBZ y otros formatos) con soporte para Zoom, Pan, vista en modo Mosaico/Galería de páginas y ajuste automático a la pantalla.
   - Lector de libros en formato EPUB con personalización de tipo de fuente, tamaño de letra, interlineado y márgenes.

2. **Biblioteca Local y Gestión de Colecciones**
   - Importación y visualización de archivos almacenados localmente en el dispositivo.
   - Creación, edición y organización de categorías o colecciones personalizadas para ordenar cómics y libros.

3. **Integración con Almacenamiento en la Nube**
   - Conexión directa e integración con **Google Drive** y **Microsoft OneDrive** para explorar, importar y leer archivos directamente desde la nube.

4. **Experiencia Visual y Personalización de Lectura**
   - Modos y esquemas de color de lectura: Oscuro, Gris, Blanco y Sepia, junto con control directo de brillo dentro de la app.
   - Soporte para modo Claro y Oscuro global y personalización de temas con paletas predeterminadas y un selector de color personalizado (*Custom Color Picker*).

---

## High-Level Tech Stack

- **Lenguaje Principal:** Kotlin
- **UI Framework:** Jetpack Compose con **Material Design 3 (M3)**
- **Navegación:** **Jetpack Navigation 3** (arquitectura de navegación impulsada por estado)
- **Estrategia Adaptativa:** **Compose Material Adaptive** (`androidx.compose.material3.adaptive`) para proporcionar layouts adaptativos según el tamaño de pantalla (teléfonos, tabletas y plegables)
- **Concurrencia y Manejo de Estado:** Kotlin Coroutines y `StateFlow` / `SharedFlow`
- **Arquitectura:** MVVM (Model-View-ViewModel) con Unidirectional Data Flow (UDF)
- **Procesamiento de Archivos y Lectura:**
  - Lector/Descompresor de archivos comprimidos para formatos de cómics (CBR/CBZ).
  - Parser/Renderizador de documentos EPUB para libros.
- **Integraciones Cloud & Storage:**
  - Storage Access Framework (SAF) para acceso local a archivos.
  - SDK / APIs REST para autenticación y exploración de archivos en Google Drive y Microsoft OneDrive.

## Implementation Steps
**Total Duration:** 39m 27s

### Task_1_CoreAndLibraryManagement: Setup project architecture, Room database for comics/books library and categories, SAF local file picker, and library UI with Jetpack Navigation 3 and Compose Adaptive.
- **Status:** COMPLETED
- **Updates:** Room database, entities (MediaItem, Category), LibraryRepository, FileImportHelper with SAF URI persistence & cover extraction, LibraryViewModel, and Material Design 3 Library UI with search, filtering, category creation, and grid/list view successfully implemented and verified with assembleDebug.
- **Acceptance Criteria:**
  - Project builds successfully
  - Room database and repository for books/comics and categories configured
  - SAF file picker integrated to import local CBR, CBZ, and EPUB files
  - Library UI displays imported books/comics, categorized list, and adaptive navigation structure
- **Duration:** 11m 13s

### Task_2_ComicAndEpubReaders: Implement reader engines for CBR/CBZ (archive decompression, zoom/pan interactive viewer, page gallery) and EPUB (HTML/text parser, styling options for font, size, line spacing, margins).
- **Status:** COMPLETED
- **Updates:** Implemented Comic reader engine (CBZ/CBR/PDF extraction, ZoomableImage with gesture pinch/pan/double-tap, page gallery grid, progress auto-saving) and EPUB reader engine (parser, XHTML text rendering, font family/size/line-spacing/margin controls, TOC bottom sheet). Integrated both screens with Library navigation. Build and unit tests verified cleanly.
- **Acceptance Criteria:**
  - CBZ/CBR files extracted and displayed with interactive zoom and pan
  - EPUB files parsed and text rendered with customizable font, size, line spacing, and margins
  - Page gallery/mosaic view implemented for comic pages
  - Reader UI connects to library items
- **Duration:** 8m 50s

### Task_3_CloudStorageAndThemes: Integrate Google Drive and Microsoft OneDrive file browsing/downloading, and implement theme customization (Light/Dark/Sepia/Grey modes, Custom Color Picker, brightness control).
- **Status:** COMPLETED
- **Updates:** Implemented theme customization with Light/Dark modes, preset color palettes, HSV/RGB custom color picker, reading background schemes (Blanco, Sepia, Gris, Oscuro), and in-app screen brightness control. Implemented Cloud Storage integration for Google Drive and Microsoft OneDrive file browsing and importing into library, along with a Settings screen. Verified with assembleDebug and 13 unit tests passed.
- **Acceptance Criteria:**
  - Google Drive and OneDrive cloud integrations enable browsing and importing remote files
  - Reading modes (Dark, Grey, Light, Sepia) and Custom Color Picker update UI theme
  - In-app brightness control adjusts screen brightness dynamically
- **Duration:** 7m 42s

### Task_4_RunAndVerify: Final integration, build verification, stability testing across screens, and instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Final verification completed by critic_agent. App deployed and verified on emulator with 0 crashes, 0 ANRs, clean build, and all unit tests passing.
- **Acceptance Criteria:**
  - build pass
  - make sure all existing tests pass
  - app does not crash
  - critic_agent verifies application stability, requirement alignment, and UI quality
- **Duration:** 11m 42s

