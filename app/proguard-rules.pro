# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ==================== Retrofit ====================
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when available.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and). R8 full mode requires that interfaces be kept.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept., while the, Retrofit uses the signature to instantiate types for call adapters.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# ==================== OkHttp ====================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ==================== Gson ====================
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# ==================== App API Models ====================
# Keep all API model classes
-keep class com.kaixuan.weightloss.api.** { *; }
-keepclassmembers class com.kaixuan.weightloss.api.** { *; }

# Keep ApiService interface
-keep interface com.kaixuan.weightloss.api.ApiService { *; }

# Keep data classes with their fields
-keepclassmembers class com.kaixuan.weightloss.api.AuthResponse { *; }
-keepclassmembers class com.kaixuan.weightloss.api.AuthData { *; }
-keepclassmembers class com.kaixuan.weightloss.api.LoginRequest { *; }
-keepclassmembers class com.kaixuan.weightloss.api.RegisterRequest { *; }
-keepclassmembers class com.kaixuan.weightloss.api.RefreshTokenResponse { *; }
-keepclassmembers class com.kaixuan.weightloss.api.RefreshTokenData { *; }
-keepclassmembers class com.kaixuan.weightloss.api.SimpleResponse { *; }
-keepclassmembers class com.kaixuan.weightloss.api.UserSettingsResponse { *; }
-keepclassmembers class com.kaixuan.weightloss.api.UserSettingsData { *; }
-keepclassmembers class com.kaixuan.weightloss.api.UpdateSettingsRequest { *; }
-keepclassmembers class com.kaixuan.weightloss.api.WeightRecordResponse { *; }
-keepclassmembers class com.kaixuan.weightloss.api.WeightRecordData { *; }
-keepclassmembers class com.kaixuan.weightloss.api.WeightRecordsListResponse { *; }
-keepclassmembers class com.kaixuan.weightloss.api.WeightRecordsResponse { *; }
-keepclassmembers class com.kaixuan.weightloss.api.WeightStatistics { *; }
-keepclassmembers class com.kaixuan.weightloss.api.AddWeightRequest { *; }
-keepclassmembers class com.kaixuan.weightloss.api.ChangePasswordRequest { *; }
