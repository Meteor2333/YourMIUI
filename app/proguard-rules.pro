-repackageclasses
-allowaccessmodification
-overloadaggressively

-keep @cn.coderstory.miwater.annotation.DontObfuscate class *

-keepclassmembers class * {
    @cn.coderstory.miwater.annotation.DontObfuscate <fields>;
}

-keepclassmembers class * {
    @cn.coderstory.miwater.annotation.DontObfuscate <methods>;
}