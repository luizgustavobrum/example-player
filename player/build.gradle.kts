plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.lgsb.player"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        isCoreLibraryDesugaringEnabled = true
    }

}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    api(libs.androidx.exoplayer)
    api(libs.androidx.exoplayer.ui)
    implementation(libs.androidx.exoplayer.dash)
    implementation(libs.androidx.exoplayer.hls)
    implementation(libs.androidx.exoplayer.ima)
    implementation(libs.ads.interactivemedia)

}