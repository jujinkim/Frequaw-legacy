# Frequaw
Frequently used applications icon launcher Android widget app. [#PlayStore](https://play.google.com/store/apps/details?id=com.jujinkim.frequaw)

Frequaw is an Android home screen widget that tracks and displays your most frequently used applications, making it easier to launch your favorite apps quickly.

## Features

- 📊 **Multiple tracking modes**: Track apps by launch count or usage time
- 🎨 **Highly customizable**: Adjust icon size, colors, transparency, and layout
- 📦 **Icon pack support**: Use custom icon packs for a personalized look
- 📌 **Pin favorite apps**: Keep important apps always visible
- 🌓 **Dark mode support**: Separate color settings for light and dark themes
- 📱 **Multiple widgets**: Create different widgets with individual settings
- 💾 **Backup & Restore**: Export and import your settings
- 🔒 **Privacy focused**: All data stays on your device

## Requirements

- Android 6.0 (API 23) or higher
- Permissions:
  - `QUERY_ALL_PACKAGES` - To list installed applications
  - `PACKAGE_USAGE_STATS` - For usage time tracking mode
  - `ACCESSIBILITY_SERVICE` - For launch count tracking mode

## Project Structure

```
Frequaw-legacy/
├── app/                           # Main application module
│   ├── src/main/java/.../frequaw/
│   │   ├── adapter/              # RecyclerView adapters
│   │   ├── applist/              # App list management
│   │   │   ├── AppListManager.kt
│   │   │   ├── AppListRepoAccessibilityService.kt
│   │   │   ├── AppListRepoUsageStats.kt
│   │   │   └── AppListRepoRecent.kt
│   │   ├── data/                 # Data persistence layer
│   │   │   ├── FrequawData.kt
│   │   │   └── FrequawDataHelper.kt
│   │   ├── model/                # Data models
│   │   ├── theme/                # Theme management
│   │   ├── ui/                   # UI components (Compose + Views)
│   │   │   └── Settings*.kt      # Settings screen composables
│   │   ├── viewmodel/            # MVVM ViewModels
│   │   ├── widget/               # Widget implementation
│   │   │   ├── FrequawWidget.kt
│   │   │   └── FrequawWidgetUtils.kt
│   │   ├── FrequawApp.kt         # Application class
│   │   └── MainActivity.kt        # Main settings activity
│   └── src/main/res/             # Resources
├── gradle/                       # Gradle wrapper
└── etc/                         # Store assets
```

## Development Guide

### Architecture
The project follows **MVVM architecture** with:
- **ViewModels** for business logic
- **LiveData/Flow** for reactive programming
- **Repository pattern** for data access
- **Jetpack Compose** for modern UI (settings screens)
- **Traditional Views** for widget UI

### Key Components

#### App Tracking
Different repositories handle app usage tracking:
- `AppListRepoAccessibilityService` - Tracks app launches via accessibility service
- `AppListRepoUsageStats` - Uses Android's UsageStatsManager for time-based tracking
- `AppListRepoRecent` - Tracks recently used apps

#### Data Persistence
- `FrequawData` - Main data model
- `FrequawDataHelper` - Handles save/load with Gson serialization
- Supports data migration from older versions

#### Widget Management
- `FrequawWidget` - AppWidgetProvider implementation
- `FrequawWidgetUtils` - Helper functions for widget updates
- Each widget instance can have independent settings

### Building the Project

```bash
# Clone the repository
git clone https://github.com/jujinkim/Frequaw-legacy.git
cd Frequaw-legacy

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Run tests
./gradlew test

# Run lint checks
./gradlew lint
```

### Development Setup

1. Open the project in Android Studio
2. Sync project with Gradle files
3. Run on device/emulator with Android 6.0+

### Code Style
- Kotlin official code style
- Use provided `.editorconfig` settings
- Run lint checks before committing

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and lint checks
5. Submit a pull request

For bug reports and feature requests, please use the [issue tracker](https://github.com/jujinkim/Frequaw-legacy/issues).

## Maintainance
By [Jujin Kim](https://jujinkim.com) @ [Cozelsil](https://cozelsil.com) and some contributors.

## Sponsoring
If you love this project, just  
[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/jujin)

## License
Frequaw is a free and open-source project under the [MIT License](../main/LICENSE).

### Why MIT?

The MIT license was chosen because:
- ✅ **Simple and permissive** - Easy to understand and use
- ✅ **Minimal restrictions** - Maximum freedom for users and contributors
- ✅ **Wide compatibility** - Works well with other open source licenses
- ✅ **Allows commercial use** - Important for Play Store distribution
- ✅ **Community friendly** - Encourages contributions and adoption

This means:
- 🔧 You can use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
- 💰 You can use it in commercial projects
- 📝 You must include the copyright notice and license in all copies
- ⚖️ The software is provided "as is" without warranty

See the [LICENSE](../main/LICENSE) and [NOTICE](../main/NOTICE) files for details.

---

<div align="center">
  
### Developed by [Cozelsil](https://cozelsil.com)
Personal digital service/content studio by Jujin Kim

</div>
