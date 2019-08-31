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

#LitePal 数据库---------------------------------------------------------------------------------
-keep class org.litepal.** {
    *;
}

-keep class * extends org.litepal.crud.DataSupport {
    *;
}
#LitePal 数据库---------------------------------------------------------------------------------


# Gson-----------------------------------------------------------------------------------------
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod
# 需要替换实体类包名路径
-keep class com.zf.lose.weight.dengyq.bean.** { *; }
# Gson--------------------------------------------------------------------------

# eventbus------------------------------------------------------------------
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# eventbus------------------------------------------------------------------


#com.haibin:calendarview 日历控件------------------------------------------------------------------
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
#com.haibin:calendarview 日历控件------------------------------------------------------------------


# 友盟------------------------------------------------------------------------------------------------
-keep class com.umeng.** {*;}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 这里需要替换包名
-keep public class [com.zf.lose.weight].R$*{
public static final int *;
}

# 友盟------------------------------------------------------------------------------------------------

# 友盟特有-----------------------------------------------------------------------------------------------------------------
# 填应用包名
-keep public class [com.allever.security.photo.browser].R$*{
    public static final int *;
}

# EventBus--------------------------------------------------------------------------------------------------------------
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
# EventBus----------------------------------------------------------------------------------

# Glide----------------------------------------------------------------------------------------------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# for DexGuard only
-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
# Glide----------------------------------------------------------------------------------
