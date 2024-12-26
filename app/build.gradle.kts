plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.jads"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.jads"
        minSdk = 23
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // If you're using version catalogs, use the alias from the catalog
    implementation(libs.constraintlayout) // This assumes you have the version catalog set up properly

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
