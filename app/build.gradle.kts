plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.s23010421.busmate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.s23010421.busmate"
        minSdk = 24
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
    // AndroidX Core Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Google Maps and Location Services
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.androidx.appcompat)
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Glide for Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // Media
    implementation("androidx.media:media:1.7.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")



}