# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Frequaw is an Android widget application that provides a frequently used applications icon launcher. It's built with Kotlin and uses both Jetpack Compose and traditional Android Views for UI.

## Key Architecture

### MVVM Pattern
- ViewModels: `SettingViewModel`, `WidgetModel`
- LiveData and Flow for reactive programming
- Repository pattern for data access with multiple app list sources

### Main Components
- **Widget**: `FrequawWidget.kt` - AppWidgetProvider implementation
- **App Detection**: Multiple modes via different repositories (Accessibility Service, Usage Stats, Recent Apps)
- **Settings UI**: Jetpack Compose components in `ui/settings/` package
- **Data Persistence**: `FrequawData.kt` with Gson serialization

## Essential Commands

### Build & Run
```bash
./gradlew assembleDebug         # Build debug APK
./gradlew installDebug          # Install on device/emulator
./gradlew assembleRelease       # Build release APK (requires signing)
```

### Testing
```bash
./gradlew test                  # Run all unit tests
./gradlew connectedAndroidTest  # Run instrumented tests on device
./gradlew lint                  # Run lint checks
```

### Development
```bash
./gradlew clean build           # Clean and full build
./gradlew dependencies          # Show dependency tree
```

## Code Organization

### Package Structure
- `com.jujinkim.frequaw.applist/` - App list management with different data sources
- `com.jujinkim.frequaw.ui/` - UI components (Compose and View-based)
- `com.jujinkim.frequaw.widget/` - Widget implementation and utilities
- `com.jujinkim.frequaw.data/` - Data persistence layer
- `com.jujinkim.frequaw.model/` - Data models

### Key Features to Understand
1. **Multiple App Detection Modes**: The app can track app usage through Accessibility Service or Usage Stats API
2. **Widget Instances**: Supports multiple widgets with independent settings
3. **Icon Pack Support**: Can use custom icon packs via `IconHelper`
4. **Data Migration**: `FrequawOldDataUpgrader` handles backward compatibility

## Important Technical Details

- **Min SDK**: 23 (Android 6.0)
- **Target SDK**: 35 (Android 15)
- **Permissions**: Requires `QUERY_ALL_PACKAGES`, `PACKAGE_USAGE_STATS`, and accessibility service permissions
- **ProGuard**: Custom rules for Gson serialization in release builds
- **Localization**: Supports Korean and Portuguese

## Development Tips

1. When modifying widget behavior, test with multiple widget instances
2. App list repositories have different accuracy/battery trade-offs
3. Settings changes should update widgets via `FrequawWidgetUtils.updateAllWidget()`
4. Use `FrequawApp.context` for global context access
5. Data persistence uses Gson - ensure models are properly annotated for ProGuard