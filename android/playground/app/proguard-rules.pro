# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/lixinke/Tool/android-eclipse/adt-bundle-mac-x86_64-20140702/sdk/tools/proguard/proguard-android.txt
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
##weex
-keep class cn.xxzhushou.xmod.wxui.bridge.**{*;}
-keep class cn.xxzhushou.xmod.wxui.dom.**{*;}
-keep class cn.xxzhushou.xmod.wxui.adapter.**{*;}
-keep class cn.xxzhushou.xmod.wxui.common.**{*;}
-keep class * implements cn.xxzhushou.xmod.wxui.IWXObject{*;}
-keep class cn.xxzhushou.xmod.wxui.ui.**{*;}
-keep class cn.xxzhushou.xmod.wxui.ui.component.**{*;}
-keep class cn.xxzhushou.xmod.wxui.utils.**{
    public <fields>;
    public <methods>;
    }
-keep class cn.xxzhushou.xmod.wxui.view.**{*;}
-keep class cn.xxzhushou.xmod.wxui.module.**{*;}
-keep public class * extends cn.xxzhushou.xmod.wxui.common.WXModule{*;}