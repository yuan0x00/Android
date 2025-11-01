# WebView相关的保留规则
-keep class android.webkit.** { *; }
-keep class org.chromium.** { *; }
-keep class com.google.android.gms.common.** { *; }

# 保留所有Activity、Service等组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Application

# 保留View相关的类和方法
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# 保留注解
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# 保留资源ID
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 保留Parcelable序列化
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# 保留JNI方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}