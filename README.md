# Keyboard Studio

Colorful Gradient Android app for designing and previewing keyboard themes.

## MVP
- Browse starter visual themes.
- Preview a keyboard mockup inside the app.
- Choose gradient palettes.
- Open Android keyboard settings and read compatibility guidance.

## Android compatibility
A regular Android app cannot remotely restyle Gboard, Samsung Keyboard, Microsoft SwiftKey, or every third-party IME. Keyboard Studio currently creates and previews designs inside its own UI and provides a shortcut to system keyboard settings. Applying a theme to another keyboard is possible only where that keyboard exposes a supported import/theme mechanism. The app does not claim universal third-party theme installation.

## Build
Open this repository in Android Studio, sync Gradle, and run the `app` configuration.

- Package: `com.keyboardstudio.app`
- Minimum Android: 8.0 (API 26)
- UI: Kotlin + Jetpack Compose
