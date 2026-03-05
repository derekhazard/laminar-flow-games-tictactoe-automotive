# Laminar Flow Games — Tic-Tac-Toe for Android Automotive OS

![CI](https://github.com/derekhazard/laminar-flow-games-tictactoe-automotive/actions/workflows/ci.yml/badge.svg)
![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg)

A native Android Automotive OS (AAOS) Tic-Tac-Toe game targeting API 34 (Android 14),
part of the Laminar Flow Games open source collection.

---

## Features

- **Two-player** local multiplayer
- **AI opponent** — minimax algorithm with 1-second move delay
- **1 Player / 2 Player mode toggle** — switch modes any time
- **In-session scorekeeping** — tracks wins for each player and draws
- **Auto-clear board** — board resets 3 seconds after a win or draw
- **Win flash animation** — winning line flashes on victory
- **Dark cockpit theme** — low-glare UI designed for automotive displays
- **Park-only enforcement** — `CarUxRestrictions` API disables board cells and mode toggle while driving

## Roadmap

- [x] Project setup
- [x] Two-player local game
- [x] AI opponent (minimax algorithm)
- [x] In-session scorekeeping
- [ ] Google Play Developer account signup
- [ ] Internal testing on AAOS hardware (Cadillac Lyriq)
- [ ] Closed testing (12 testers, 14 days)
- [ ] Production release on Google Play

---

## Prerequisites

- Android Studio Otter 3 Feature Drop (2025.2.3) or later
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
| pre-commit | detekt | Static analysis, code smells, KDoc enforcement |
| pre-push | Android Lint | AAOS API issues, manifest errors |

### Unit Tests

- Framework: [Robolectric](https://robolectric.org/) (JVM-based, no emulator required)
- Coverage: JaCoCo with 80% minimum on the `game/` package
- Run: `./gradlew testDebugUnitTest`

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

docs/
├── decisions/                 # Architecture Decision Records (ADRs)
├── play-store-listing.md      # Store listing copy
├── privacy-policy.md          # Privacy policy
└── store-assets/              # Play Store graphic assets

scripts/                       # Asset generation scripts (Python)
```

## Store Assets

Play Store graphic assets live in `docs/store-assets/`. To regenerate them:

> **Note:** The commands below assume macOS with Homebrew. On Linux, replace
> `DYLD_LIBRARY_PATH` with `LD_LIBRARY_PATH` or omit it if cairo is installed system-wide.

```bash
pip install -r scripts/requirements.txt
DYLD_LIBRARY_PATH=/opt/homebrew/lib python scripts/generate_icon.py
DYLD_LIBRARY_PATH=/opt/homebrew/lib python scripts/generate_feature_graphic.py
DYLD_LIBRARY_PATH=/opt/homebrew/lib python scripts/generate_phone_screenshots.py
```

---

## License

Copyright 2026 Derek Hazard

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
