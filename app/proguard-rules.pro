# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\pf\Sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 混淆算法
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/* # 不对算术和类型转化操作符优化，不改变类层次

# 压缩
-optimizationpasses 5 # 指定压缩级别

# 优化
-dontoptimize # 不优化输入的类文件
-allowaccessmodification # 优化时允许访问并修改有修饰符的类和类的成员

#Bean类
#-keep public class com.lga.dailyread.bean.**{*;} #  保持Bean类
-keepclassmembers class com.lga.dailyread.bean.** { *; } # 保持Bean类，但混淆类名

# 引用库--------------
# fastjson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# com.android.support:design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

# com.android.support:appcompat-v7
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}
# end--------------

# 测试
# 保持测试相关的代码
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**

# 混淆
-dontskipnonpubliclibraryclassmembers # 不跳过非公共的库的类成员
-overloadaggressively # 混淆时应用侵入式重载
-useuniqueclassmembernames # 把混淆类中的方法名也混淆
-renamesourcefileattribute SourceFile # 将文件来源重命名为“SourceFile”字符串