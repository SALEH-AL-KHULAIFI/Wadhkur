plugins { id("com.android.application") }
android { namespace="com.saleh.wadhkur"; compileSdk=36
 defaultConfig { applicationId="com.saleh.wadhkur"; minSdk=23; targetSdk=35; versionCode=200; versionName="2.0.0" }
 buildTypes { release { isMinifyEnabled=false } }
}
