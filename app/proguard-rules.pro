-repackageclasses
-allowaccessmodification
-overloadaggressively

-keep @androidx.annotation.Keep class *

-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclassmembers class * {
    @androidx.annotation.Keep <methods>;
}