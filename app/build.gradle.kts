import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

/**
 * Reads the OpenWeather key from `local.properties`, which is git-ignored.
 *
 * Falls back to the `OPENWEATHER_API_KEY` environment variable so CI can inject
 * it without a checked-in file. When neither is present the build still succeeds
 * with an empty key and the app shows a "key not configured" screen, so a fresh
 * clone compiles without secrets.
 */
val openWeatherApiKey: String = run {
    val localProperties = rootProject.file("local.properties")
    val fromFile = if (localProperties.exists()) {
        Properties().apply { localProperties.inputStream().use(::load) }
            .getProperty("OPENWEATHER_API_KEY")
    } else {
        null
    }
    val key = fromFile ?: System.getenv("OPENWEATHER_API_KEY") ?: ""
    if (key.isBlank()) {
        logger.warn(
            "Tenky: no OPENWEATHER_API_KEY found. Add it to local.properties " +
                "(see local.properties.example) or export it as an environment variable.",
        )
    }
    key
}

android {
    namespace = "fr.dutapp.tenky"
    compileSdk = 37

    defaultConfig {
        applicationId = "fr.dutapp.tenky"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$openWeatherApiKey\"")
        // Host root: the service declares full paths, since the weather data
        // lives under data/2.5/ and geocoding under geo/1.0/.
        buildConfigField("String", "OPENWEATHER_BASE_URL", "\"https://api.openweathermap.org/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
