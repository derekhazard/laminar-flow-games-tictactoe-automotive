plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    jacoco
}

android {
    namespace = "com.agongames.tictactoe"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.agongames.tictactoe"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

detekt {
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
}

// Enforce 80% instruction coverage on the game logic package.
// Passes trivially until game/ classes exist; enforces once step 4 is merged.
tasks.register<JacocoCoverageVerification>("jacocoGameCoverageVerification") {
    dependsOn("testDebugUnitTest")
    violationRules {
        rule {
            includes = listOf("com.agongames.tictactoe.game.*")
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        },
    )
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            include("**/game/**")
        },
    )
    sourceDirectories.setFrom(files("src/main/java"))
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
