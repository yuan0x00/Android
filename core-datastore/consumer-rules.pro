# Consumer ProGuard rules for core-datastore
# Keep the interface and its methods
-keep interface com.core.datastore.IDataStore { *; }
-keep class com.core.datastore.DefaultDataStore { *; }

# Keep MMKV classes
-keep class com.tencent.mmkv.** { *; }
-dontwarn com.tencent.mmkv.**