# 002 — Test strategy: JUnit 4 + MockK, no instrumentation tests

**Date:** 2026-02-23
**Status:** Accepted

## Context

The project needs a test strategy before game logic classes are implemented
in step 4. Two categories of tests are possible on Android:

1. **JVM unit tests** (`src/test/`) — run on the local JVM, no emulator needed,
   fast (milliseconds per test).
2. **Instrumentation tests** (`src/androidTest/`) — run on a real device or
   emulator, slow (seconds per test), require an Android runtime.

The game logic classes (`GameBoard`, `GameRules`) are pure Kotlin with no
Android framework dependencies. They are naturally suited to JVM unit tests.

AAOS complicates instrumentation testing: the `AAOS_API34` emulator takes
several minutes to boot in CI, there is no standard touch-screen input driver
for automated UI interaction, and `CarUxRestrictionsManager` behaviour requires
a running automotive service stack that is not available in a standard emulator
on GitHub Actions `ubuntu-latest`.

## Decision

Use **JUnit 4** (already in the dependency catalog) and **MockK** for all
unit tests. Do not add instrumentation tests at this stage.

**MockK** is preferred over Mockito because it works natively with Kotlin
classes, data classes, objects, and companion objects without requiring `open`
modifiers or a Mockito Kotlin extension.

**JaCoCo** (AGP built-in) enforces a minimum of **80% instruction coverage**
on the `com.agongames.tictactoe.game.*` package. This threshold applies only
to game logic, not to Android Activity/UI code, which is impractical to unit
test without an instrumentation harness.

The coverage verification task (`jacocoGameCoverageVerification`) passes
trivially until `game/` classes exist, then enforces the threshold from
step 4 onward.

## Consequences

- Game logic must be written as pure Kotlin with no Android imports to remain
  unit-testable on the JVM.
- `GameActivity` and layout wiring are excluded from the coverage threshold.
- If a future step requires testing `CarUxRestrictionsManager` behaviour, a
  fake/test double should be introduced rather than adding instrumentation tests.
- Instrumentation tests can be added later if UI regression testing becomes a
  priority; this ADR should be superseded at that point.
