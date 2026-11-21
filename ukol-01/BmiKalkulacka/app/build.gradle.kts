plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "cz.vspj.pmp.bmikalkulacka"
    compileSdk = 37

    defaultConfig {
        applicationId = "cz.vspj.pmp.bmikalkulacka"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        // AGP is intentionally pinned to a version supported by Android Studio 2026.2.
        disable += "AndroidGradlePluginVersion"
        warningsAsErrors = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso.core)
}
