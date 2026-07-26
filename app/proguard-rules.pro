# Project-specific ProGuard rules.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers so crash reports stay readable, but hide the original
# source file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx.serialization -------------------------------------------------
# The compiler plugin generates a Companion.serializer() for every @Serializable
# class. R8 cannot tell those are reachable, so keep them for the network DTOs
# and for the models persisted to DataStore.
-keepclassmembers class fr.dutapp.tenky.** {
    *** Companion;
}
-keepclasseswithmembers class fr.dutapp.tenky.** {
    kotlinx.serialization.KSerializer serializer(...);
}
