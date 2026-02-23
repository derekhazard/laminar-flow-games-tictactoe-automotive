# 001 — Do not apply the kotlin-android plugin

**Date:** 2026-02-22
**Status:** Accepted

## Context

AGP 9.0.1 bundles Kotlin 2.2.10 and registers the `kotlin` Gradle extension
internally when `com.android.application` is applied. Explicitly applying
`org.jetbrains.kotlin.android` alongside AGP 9.0.1 triggers a fatal build
error at configuration time:

```text
Cannot add extension with name 'kotlin', as there is an extension already
registered with that name.
```

Additionally, `kotlinOptions { jvmTarget = "11" }` is only available through
the kotlin-android plugin DSL and causes an "Unresolved reference" error once
the plugin is removed.

## Decision

Do not apply `org.jetbrains.kotlin.android` in `build.gradle.kts` or
`app/build.gradle.kts`. Let AGP manage the Kotlin toolchain entirely.

Set the JVM target through the standard Java compile options instead:

```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
```

AGP 9.x propagates this target to the Kotlin compiler automatically
([Migrate to built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)).

## Consequences

- Build succeeds without any workarounds or version pinning.
- The kotlin-android plugin alias is not declared in `libs.versions.toml`.
- If a future AGP version removes the bundled Kotlin integration and requires
  the plugin again, `compileOptions` + `kotlinOptions` will need to be
  re-introduced and this ADR superseded.
