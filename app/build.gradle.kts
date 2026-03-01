import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    jacoco
}

val localProperties =
    Properties().also { props ->
        rootProject.file("local.properties").takeIf { it.exists() }
            ?.reader()?.use { props.load(it) }
    }

android {
    namespace = "com.laminarflowgames.tictactoe"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.laminarflowgames.tictactoe"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystoreFile = localProperties.getProperty("UPLOAD_KEYSTORE_FILE")
    val keystorePassword = localProperties.getProperty("UPLOAD_KEYSTORE_PASSWORD")
    val keyAliasValue = localProperties.getProperty("UPLOAD_KEY_ALIAS")
    val keyPasswordValue = localProperties.getProperty("UPLOAD_KEY_PASSWORD")

    val hasReleaseSigningProps =
        !keystoreFile.isNullOrBlank() &&
            !keystorePassword.isNullOrBlank() &&
            !keyAliasValue.isNullOrBlank() &&
            !keyPasswordValue.isNullOrBlank()

    if (hasReleaseSigningProps) {
        signingConfigs {
            create("release") {
                storeFile = file(keystoreFile!!)
                storePassword = keystorePassword
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
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
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
    useLibrary("android.car")
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
            includes = listOf("com.laminarflowgames.tictactoe.game.*")
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
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
