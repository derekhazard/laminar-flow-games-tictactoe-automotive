# 003 — Park-only enforcement via CarUxRestrictions system API

**Date:** 2026-02-23
**Status:** Accepted

## Context

AAOS requires that apps which could distract the driver restrict interaction
while the vehicle is in motion. The Tic-Tac-Toe game board is a distraction
risk; cells must be non-interactive unless the vehicle is parked.

Two approaches exist:

1. **Car App Library** (`androidx.car.app`) — the recommended library for new
   apps targeting the Play Store for Automotive. It enforces driving restrictions
   automatically for its own template-based UI.
2. **`android.car` system API** (`CarUxRestrictionsManager`) — the lower-level
   system API that gives direct control over restriction callbacks.

The Car App Library's template system does not support a free-form 3×3 grid
view, which is central to the Tic-Tac-Toe UX. Adopting it would require
replacing the custom `GridLayout` board with a list or pane template that
cannot represent the game faithfully. The `android.car` system API is therefore
the correct choice for this app.

## Decision

Use `CarUxRestrictionsManager` from `android.car.drivingstate` to listen for
driving state changes and gate cell clicks accordingly.

### useLibrary("android.car")

`android.car` is registered as an optional platform library in the Android SDK
`optional.json` for API 29+. AGP's `useLibrary("android.car")` declaration
inside the `android {}` block instructs the toolchain to add the jar to the
compile classpath automatically, without needing to locate the jar by path.
At runtime the system provides the real classes on the AAOS device.

### New Game button is not gated

The "New Game" button resets game state in a single tap. It involves no
multi-step interaction and poses negligible distraction risk. Gating it while
driving would frustrate passengers who want to start a new game while parked
at a traffic light or while the driver is focused on the road. This matches the
AAOS distraction-optimisation guideline that simple, single-action controls
may remain available.

### Broad Exception catch in initCarUxRestrictions

`Car.createCar(Context)` can throw on standard (non-automotive) Android
environments when the Car service is not present. This includes:

- Standard Android phones used during development
- JVM unit test environments
- Instrumented test environments without Car service

Catching `Exception` broadly allows the activity to initialise successfully on
all these environments. The graceful degradation strategy is to leave
`isDrivingRestricted = false`, keeping the board interactive. An automotive
device running the full AAOS stack will always succeed and the catch block will
never execute in production.

### Car.createCar API

`Car.createCar(Context)` was deprecated in API level 31. The non-deprecated
replacement, `Car.createCar(Context, Handler, long, CarServiceLifecycleListener)`,
is available from API 29 — matching this app's `minSdk`. This implementation
uses the lifecycle listener variant with `Handler(Looper.getMainLooper())` and
`Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT`. The listener callback fires on the main
thread when the Car service is ready, at which point `CarUxRestrictionsManager`
is obtained and the UX restrictions listener is registered. On non-automotive
devices the Car service is unavailable; `Car.createCar()` either throws or the
lifecycle callback never fires, and the board remains interactive via the
graceful-degradation catch block.

## Consequences

- Board cells are disabled whenever `CarUxRestrictions.isRequiresDistractionOptimization`
  returns true, satisfying AAOS distraction guidelines.
- The New Game button remains enabled at all times.
- On non-automotive devices (developer machines, CI emulators) the activity
  initialises without the Car service and the board is fully interactive.
- If a future AAOS API requires migration away from `android.car` system API,
  this ADR should be superseded.
