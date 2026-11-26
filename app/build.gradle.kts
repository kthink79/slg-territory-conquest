plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.game.slg"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.game.slg"
        minSdk = 26
        targetSdk = 35
        versionCode = 10
        versionName = "0.10.0"

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
    kotlinOptions {
        jvmTarget = "17"
    }
    // 로컬·CI 가 같은 debug keystore 로 서명해야 기존 설치 위에 업데이트된다.
    // 저장소에 동봉한 app/debug.keystore(표준 Android debug 키)를 우선 쓰고,
    // 없으면 홈 디렉터리 keystore 로 넘어간다.
    signingConfigs {
        val repoKeystore = file("debug.keystore")
        val homeDir = System.getenv("HOME") ?: System.getProperty("user.home")
        val homeKeystore = file("$homeDir/.android/debug.keystore")
        val keystore = when {
            repoKeystore.exists() -> repoKeystore
            homeKeystore.exists() -> homeKeystore
            else -> null
        }
        if (keystore != null) {
            getByName("debug") {
                storeFile = keystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material3:material3")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
