# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android\sdk/tools/proguard/proguard-android.txt
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
-dontshrink
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-dontusemixedcaseclassnames
-keepattributes *Annotation*
-dontpreverify
-verbose
-keep class android.support.**{*;}
-dontwarn android.support.v4.*
-dontwarn android.support.v7.*
-ignorewarnings

-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}
-dontwarn com.baidu.**

#make mapping unique
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
-keep class android.support.**{*;}

-keep class com.bigsing.fakemap.XposedActive{*;}

-keep class com.bigsing.fakemap.MainHook {
    public void handleLoadPackage*;
}