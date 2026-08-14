-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault

# Legacy Xposed entry.
-keep class com.bigsing.fakemap.xposed.legacy.LegacyHookEntry {
    public <init>();
    public void handleLoadPackage(...);
}

# Modern libxposed entry and resource name adaptation.
-dontwarn io.github.libxposed.annotation.**
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}

-keep class com.bigsing.fakemap.core.XposedStatus {
    public static boolean isActive();
}
