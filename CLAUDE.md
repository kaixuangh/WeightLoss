# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WeightLoss is an Android weight tracking app built with Kotlin and Jetpack Compose. Users can record daily weight, view trends in charts, set target weight, calculate BMI, and receive daily reminders.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (signed)
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

Build outputs are located in `app/build/outputs/apk/`.

## Architecture

**Pattern**: MVVM (Model-View-ViewModel)

**Key Components**:
- **MainActivity.kt**: Single activity hosting Compose UI
- **WeightViewModel.kt**: Central ViewModel managing all app state and business logic
- **Data Layer** (`data/`):
  - `WeightDatabase.kt`: Room database singleton with migration logic (v1→v2)
  - `WeightDao.kt`: Database access object for weight records
  - `WeightRecord.kt`: Entity representing a weight entry (one per day)
  - `UserSettings.kt`: DataStore-based settings repository (target weight, height, unit, reminder config)
- **UI Layer** (`ui/`):
  - `WeightScreen.kt`: Main screen with weight input and chart
  - `SettingsScreen.kt`: Settings configuration screen
  - `WeightChart.kt`: Vico-based chart component
  - `theme/`: Material 3 theme configuration
- **Background Tasks** (`reminder/`):
  - `ReminderWorker.kt`: WorkManager-based daily notification scheduler

## Data Storage

**Weight Storage**: All weights are stored in **kg** in the database, regardless of user's display unit preference. The ViewModel handles conversion to/from the user's selected unit (kg or 斤) for display and input.

**Database Schema**:
- Version 2 enforces one record per day using unique `dateKey` (YYYY-MM-DD format)
- Migration from v1→v2 consolidates multiple daily entries to latest entry per day

**Settings**: Stored in DataStore Preferences (key-value pairs), accessed via `UserSettingsRepository`

## Important Patterns

1. **Unit Conversion**: Always happens in ViewModel layer
   - Input: User enters weight → divide by `unit.factor` → store in kg
   - Output: Read from DB in kg → multiply by `unit.factor` → display to user

2. **Date Handling**: Records use both `date` (timestamp) and `dateKey` (YYYY-MM-DD string)
   - `dateKey` ensures uniqueness per day
   - Same-day entries overwrite previous entries

3. **State Management**: All UI state flows through ViewModel's StateFlow properties
   - `records`: List of weight records for selected time range
   - `settings`: User settings (target, height, unit, reminder)
   - `latestWeight`: Most recent weight entry
   - `selectedRange`: Current chart time range (week/month/quarter/etc)

4. **Reminder Scheduling**: Uses WorkManager with PeriodicWorkRequest
   - Calculates initial delay to target time
   - Repeats every 24 hours
   - Cancelled when reminder disabled in settings

## API Documentation

See `docs/API.md` for backend API specification (authentication, user settings, weight records). Note: The current app is offline-only; API docs are for future backend integration.

## Dependencies

- **UI**: Jetpack Compose + Material Design 3
- **Database**: Room (with kotlin-kapt for annotation processing)
- **Charts**: Vico (compose + compose-m3)
- **Preferences**: DataStore
- **Background Work**: WorkManager
- **Build**: Gradle 8.x with Kotlin 2.0.21

## Testing

- Unit tests: `app/src/test/`
- Instrumented tests: `app/src/androidTest/`

Run tests with `./gradlew test` or `./gradlew connectedAndroidTest`.
