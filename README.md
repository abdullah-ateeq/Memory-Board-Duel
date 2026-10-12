# Memory Board Duel 🧠🃏

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-7f52ff.svg)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

An offline, competitive memory card duel Android application built with **Jetpack Compose**, **Material 3**, and modern Android architecture. Test your memory against a smart CPU opponent across a 6x6 grid of custom canvas-drawn symbols!

---

## 🌟 Key Features

* **🎮 Turn-Based AI Duels**: Compete turn-by-turn against a CPU opponent on a 6×6 board (36 tiles / 18 matching symbol pairs). Finding a pair grants an extra turn!
* **🤖 3 CPU Difficulty Levels**:
  * **Easy**: Casual play with higher chance of CPU forgetting old tiles.
  * **Normal**: Balanced challenge with realistic memory retention.
  * **Hard**: Flawless memory retention for competitive players.
* **🎨 18 Custom Canvas-Drawn Symbols**: Unique vector art drawn directly via Compose Canvas—including Star, Moon, Sun, Heart, Gem, Shield, Lightning, Crown, Fire, Water, Flower, Cloud, Bell, Key, Anchor, Trophy, Diamond, and Circle.
* **🌓 Theme & Material You Support**: Seamless switching between Light, Dark, System default, and Android 12+ Dynamic Wallpaper color schemes.
* **📊 Statistics & History Tracking**: Track total games played, wins, losses, draws, best pair scores, current win streak, average pairs per match, and overall performance.
* **🔊 Custom Feedback & Animations**: Smooth card flips, celebratory pulses, optional sound effects, and haptic feedback.
* **♿ Adaptive & Accessible**:
  * Optional coordinate overlays (Columns A–F, Rows 1–6) for easy reference and accessibility.
  * Screen reader support with polite live-region announcements.
  * Responsive layout adapting automatically to portrait and wide/landscape screens.
* **🔒 100% Offline & Private**: Zero tracking, no accounts, no internet permissions required.

---

## 🛠 Tech Stack & Architecture

* **Language**: 100% [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
* **Navigation**: Jetpack Navigation Compose
* **State Management**: Unidirectional Data Flow (UDF) powered by `ViewModel`, `StateFlow`, and `SharedFlow`
* **Architecture**: Clean Architecture principles separating Logic, Data/Repository, and UI components:
  * `game.logic`: Pure rule engine (`MemoryBoardGameEngine`), hidden/visible tile projections, turn evaluations.
  * `game.ai`: CPU memory simulation (`CpuMemory`) & difficulty strategies (`MemoryBoardCpuStrategy`).
  * `data`: Persistence layer (`SettingsRepository`, `StatisticsRepository`, `SharedPreferencesStore`).
  * `ui`: Reactive Composables, canvas symbol drawing, animations, custom themes.
* **Minimum SDK**: 24 (Android 7.0)
* **Target SDK**: 37

---

## 📂 Project Structure

```
app/src/main/java/com/example/memoryboardduel/
├── MainActivity.kt
├── data/
│   ├── KeyValueStore.kt
│   ├── SettingsRepository.kt
│   └── StatisticsRepository.kt
├── game/
│   ├── ai/
│   │   ├── CpuMemory.kt
│   │   └── MemoryBoardCpuStrategy.kt
│   ├── logic/
│   │   ├── BoardGenerator.kt
│   │   └── MemoryBoardGameEngine.kt
│   └── model/
│       ├── BoardModels.kt
│       ├── Coordinate.kt
│       └── Symbols.kt
├── navigation/
│   └── AppNavigation.kt
├── ui/
│   ├── MemoryBoardViewModel.kt
│   ├── components/
│   │   ├── Common.kt
│   │   ├── FeedbackEffects.kt
│   │   ├── GameBoard.kt
│   │   ├── MemoryTileCard.kt
│   │   └── SymbolArt.kt
│   ├── screens/
│   │   ├── AboutScreen.kt
│   │   ├── GameScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── HowToPlaySheet.kt
│   │   ├── ResultScreen.kt
│   │   ├── SettingsScreen.kt
│   │   └── SplashScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── GameColors.kt
│       ├── Theme.kt
│       └── Type.kt
└── util/
    ├── Accessibility.kt
    └── GameTimings.kt
```

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio**: Ladybug (2024.2.1) or newer
* **JDK**: 11 or higher
* **Android SDK**: API Level 37

### Build & Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Memory-Board-Duel.git
   cd Memory-Board-Duel
   ```

2. **Build the APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

4. **Run Instrumented UI Tests**:
   ```bash
   ./gradlew connectedAndroidTest
   ```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
