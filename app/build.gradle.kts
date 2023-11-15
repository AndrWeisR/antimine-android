import org.gradle.api.tasks.testing.logging.TestExceptionFormat

val isGoogleBuild: Boolean by rootProject.extra
val isReleaseBuild: Boolean by rootProject.extra

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "dev.lucasnlm.antimine"

    defaultConfig {
        // versionCode and versionName must be hardcoded to support F-droid
        versionCode = 1705111
        versionName = "17.5.11"
        minSdk = 21
        targetSdk = 34
        compileSdk = 34
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    signingConfigs {
        create("release") {
            if (isReleaseBuild) {
                storeFile = file("../keystore")
                keyAlias = System.getenv("BITRISEIO_ANDROID_KEYSTORE_ALIAS")
                storePassword = System.getenv("BITRISEIO_ANDROID_KEYSTORE_PASSWORD")
                keyPassword = System.getenv("BITRISEIO_ANDROID_KEYSTORE_PRIVATE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            animationsDisabled = true
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    flavorDimensions.add("version")
    productFlavors {
        create("google") {
            dimension = "version"
            applicationId = "dev.lucasnlm.antimine"
            versionNameSuffix = " S"

            if (isGoogleBuild) {
                plugins.apply("com.google.gms.google-services")
                plugins.apply("com.bugsnag.android.gradle")
            }
        }

        create("googleInstant") {
            versionCode = 163
            dimension = "version"
            applicationId = "dev.lucasnlm.antimine"
            versionNameSuffix = " I"

            if (isGoogleBuild) {
                plugins.apply("com.google.gms.google-services")
            }
        }

        create("foss") {
            dimension = "version"
            // There"s a typo on F-Droid release :(
            applicationId = "dev.lucanlm.antimine"
            versionNameSuffix = " F"
        }
    }
}

val googleImplementation by configurations
val googleInstantImplementation by configurations
val fossImplementation by configurations

dependencies {
    // Dependencies must be hardcoded to support F-droid

    implementation(project(":external"))
    implementation(project(":common"))
    implementation(project(":control"))
    implementation(project(":about"))
    implementation(project(":ui"))
    implementation(project(":utils"))
    implementation(project(":preferences"))
    implementation(project(":themes"))
    implementation(project(":tutorial"))
    implementation(project(":core"))
    implementation(project(":gdx"))

    googleImplementation(project(":proprietary"))
    googleInstantImplementation(project(":proprietary"))
    googleInstantImplementation(project(":instant"))
    fossImplementation(project(":foss"))
    fossImplementation(project(":donation"))

    googleImplementation(project(":audio"))
    fossImplementation(project(":audio"))
    googleInstantImplementation(project(":audio-low"))

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2")

    // Constraint
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Google
    implementation("com.google.android.material:material:1.10.0")

    // Koin
    implementation("io.insert-koin:koin-android:3.1.2")
    testImplementation("io.insert-koin:koin-test:3.1.2")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Konfetti
    implementation("nl.dionsegijn:konfetti-xml:2.0.3")

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.core:core-ktx:1.12.0")
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.test:rules:1.5.0")
    testImplementation("androidx.test:runner:1.5.2")
    testImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("androidx.fragment:fragment-testing:1.6.2")
    testImplementation("org.robolectric:robolectric:4.5.1")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("io.mockk:mockk:1.13.5")

    // Core library
    androidTestImplementation("androidx.test:core:1.5.0")

    // AndroidJUnitRunner and JUnit Rules
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestUtil("androidx.test:orchestrator:1.4.2")
}

tasks.withType<Test>().configureEach {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }

    addTestListener(
        object : TestListener {
            override fun beforeTest(desc: TestDescriptor?) = Unit

            override fun beforeSuite(desc: TestDescriptor?) = Unit

            override fun afterSuite(
                desc: TestDescriptor,
                result: TestResult,
            ) = Unit

            override fun afterTest(
                desc: TestDescriptor,
                result: TestResult,
            ) {
                println("Executing test ${desc.name} [${desc.className}] with result: ${result.resultType}")
            }
        },
    )
}

// The following code disables Google Services when building for F-Droid
if (isGoogleBuild) {
    android.applicationVariants.configureEach {
        if (flavorName == "foss") {
            project
                .tasks
                .names
                .map { name ->
                    name.lowercase() to project.tasks.named(name)
                }
                .filter { (name, _) ->
                    name.contains("google") || name.contains("bugsnag")
                }
                .forEach { (_, task) ->
                    task.configure {
                        enabled = false
                    }
                }
        }
    }
}
