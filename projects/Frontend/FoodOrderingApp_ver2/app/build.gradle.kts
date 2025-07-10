// build.gradle (Module: app)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.10"  // ADD THIS LINE
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.foodorderingapp_ver2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.foodorderingapp_ver2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    // PRODUCT FLAVORS SECTION
    flavorDimensions += "company"
    productFlavors {
        create("companyA") {
            dimension = "company"
            applicationIdSuffix = ".companya"
            versionNameSuffix = "-companyA"
            manifestPlaceholders["appName"] = "Company A Food"
            buildConfigField("String", "COMPANY_THEME", "\"ORANGE\"")
        }

        create("companyB") {
            dimension = "company"
            applicationIdSuffix = ".companyb"
            versionNameSuffix = "-companyB"
            manifestPlaceholders["appName"] = "Company B Food"
            buildConfigField("String", "COMPANY_THEME", "\"GREEN\"")
        }

        create("companyC") {
            dimension = "company"
            applicationIdSuffix = ".companyc"
            versionNameSuffix = "-companyC"
            manifestPlaceholders["appName"] = "Company C Food"
            buildConfigField("String", "COMPANY_THEME", "\"PURPLE\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        buildConfig = true  // This enables BuildConfig
        compose = true
    }

    // REMOVE composeOptions - not needed with the plugin
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.1"
    // }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ViewModel for clean architecture
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Retrofit for HTTP requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp for networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //for encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

}