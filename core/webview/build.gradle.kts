plugins {
    alias(libs.plugins.local.android.library)
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.rapid.compose.core.webview"
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("androidx.webkit:webkit:1.14.0")
}