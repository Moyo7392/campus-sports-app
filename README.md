# Campus Sports

[![Android CI](https://github.com/Moyo7392/campus-sports-app/actions/workflows/android-ci.yml/badge.svg)](https://github.com/Moyo7392/campus-sports-app/actions/workflows/android-ci.yml)

A native Android app for organizing and running recreational sports events on campus. Students can authenticate, discover or create events, join conversations, manage their own events, and use in-game utilities such as scorekeeping, timers, and a coin flip.

## Highlights

- Email/password authentication with Firebase
- Real-time event discovery and registration
- Event creation, ownership, and participant workflows
- Event chat backed by Firestore
- Scorekeeping and sport-specific game controls
- Jetpack Compose interface with Material 3 components
- ViewModel and repository layers for UI state and data access

## Technology

- Kotlin
- Jetpack Compose and Material 3
- Android Architecture Components and ViewModel
- Firebase Authentication and Cloud Firestore
- Gradle Kotlin DSL

## Project structure

```text
android-app/app/src/main/java/edu/uta/campussports/
├── auth/       Authentication screens and state
├── data/       Firestore repositories and domain models
├── screens/    Events, chat, scorekeeping, and utility UI
├── viewmodel/  Event state and business logic
└── ui/theme/   Compose theme system
```

## Run locally

1. Open `android-app` in Android Studio.
2. Use a local Firebase project and add its Android configuration file to `android-app/app/`.
3. Enable Email/Password Authentication and Cloud Firestore.
4. Run the app on an Android emulator or device running API 24 or newer.

Do not reuse production Firebase credentials or real user data in a public demo.

## Build

```bash
cd android-app
./gradlew assembleDebug
```

The GitHub Actions workflow performs the same debug build for every push and pull request.

## Project scope and evidence

Campus Sports is a student team project. The repository preserves the implementation and commit history so individual contributions can be reviewed without presenting the work as a solo project.

Publicly verifiable evidence currently includes the complete Android source, Firebase repository layer, Compose screens, Gradle configuration, and automated build workflow. A demo video and screenshots are planned; they must use test accounts and fabricated event data.

## Roadmap

- Add a short emulator walkthrough using fabricated campus events
- Add unit tests for event capacity, registration, and ownership rules
- Add Firebase emulator integration tests for repository behavior
- Document team roles and individual ownership after all contributors approve the wording

## Status

Student team project for The University of Texas at Arlington. The repository demonstrates native Android, real-time data, authentication, and collaborative event workflows.
