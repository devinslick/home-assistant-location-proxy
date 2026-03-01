# ── Hilt ──────────────────────────────────────────────────────────────────────
# Hilt generates components at compile time; keep entry-point interfaces intact.
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# ── Gson / data models ────────────────────────────────────────────────────────
# Gson uses reflection to read/write fields; keep all serialized model classes.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# Keep the HA API model classes explicitly
-keep class com.devinslick.homeassistantlocationproxy.data.HaStateResponse { *; }
-keep class com.devinslick.homeassistantlocationproxy.data.HaAttributes { *; }

# ── Retrofit ──────────────────────────────────────────────────────────────────
-keep interface com.devinslick.homeassistantlocationproxy.network.HaApiService { *; }
-keep class retrofit2.** { *; }
-keepattributes Exceptions

# ── OkHttp ────────────────────────────────────────────────────────────────────
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Kotlin coroutines ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ── AndroidX DataStore ────────────────────────────────────────────────────────
-dontwarn androidx.datastore.**

# ── OSMDroid ──────────────────────────────────────────────────────────────────
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**
