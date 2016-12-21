# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Keep source file names, line numbers, and Parse class/method names for easier debugging
-keepattributes SourceFile,LineNumberTable
-keepnames class com.parse.** { *; }

# Required for Parse
-keepattributes *Annotation*
-keepattributes Signature
-dontwarn com.squareup.**
-dontwarn okio.**
-dontwarn okhttp3.**

-keep class com.parse.NotificationCompat.* { *; }
-dontwarn com.parse.NotificationCompat.**

-keep class com.parse.ParseApacheHttpClient.* { *; }
-dontwarn com.parse.ParseApacheHttpClient.**
-dontwarn android.app.Notification
-dontwarn android.net.SSLCertificateSocketFactory

-keep public class com.google.android.gms.* { public *; }
-keep class com.google.common.* { *; }
-dontwarn com.google.common.**
-dontwarn com.google.android.gms.**
