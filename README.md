# Laminar Flow Games — Tic-Tac-Toe for Android Automotive OS

![CI](https://github.com/derekhazard/laminar-flow-games-tictactoe-automotive/actions/workflows/ci.yml/badge.svg)
![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg)

A native Android Automotive OS (AAOS) Tic-Tac-Toe game targeting API 34 (Android 14),
part of the Laminar Flow Games open source collection.

---

## Features

- Two-player local multiplayer
- Enforces gameplay only when vehicle is in Park (`CarUxRestrictions` API)
- Designed for the AAOS 1024p landscape display

## Roadmap

- [x] Project setup
- [ ] Two-player local game
- [ ] AI opponent (minimax algorithm)
- [ ] Scorekeeping
- [ ] High score tracker

---

## Prerequisites

- Android Studio (Otter or later)
- Android SDK API 34
- Android Automotive with Google APIs arm64-v8a system image (API 34-ext9)
- JDK 17

## Getting Started

### 1. Clone the repo

```bash
git clone https://github.com/derekhazard/laminar-flow-games-tictactoe-automotive.git
cd laminar-flow-games-tictactoe-automotive
```

### 2. Open in Android Studio

Open the project root in Android Studio. Gradle will sync automatically.

### 3. Create the AAOS emulator

If you don't have an AAOS AVD, create one via the terminal:

```bash
~/Library/Android/sdk/cmdline-tools/latest/bin/avdmanager create avd \
  --name "AAOS_API34" \
  --package "system-images;android-34-ext9;android-automotive;arm64-v8a" \
  --device "automotive_1024p_landscape"
```

### 4. Build and run

```bash
./gradlew assembleDebug
```

Then launch the AAOS_API34 emulator from Android Studio's Virtual Device Manager and run the app.

---

## Code Quality

This project enforces code quality on every commit via [Lefthook](https://github.com/evilmartians/lefthook):

| Hook | Tool | What it checks |
|---|---|---|
| pre-commit | ktlint | Kotlin style and formatting |
| pre-commit | detekt | Static analysis, code smells |
| pre-push | Android Lint | AAOS API issues, manifest errors |

To auto-fix formatting violations before committing:

```bash
./gradlew ktlintFormat
```

---

## Project Structure

```
app/src/main/
├── AndroidManifest.xml        # App declaration and AAOS feature flags
├── java/com/laminarflowgames/tictactoe/
│   ├── ui/                    # Activities and UI components
│   └── game/                  # Game logic (board, rules, AI)
└── res/
    ├── layout/                # XML layouts
    ├── values/                # Strings, colors, themes
    └── drawable/              # Icons and graphics
```

---

## License

Copyright 2026 Derek Hazard

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
