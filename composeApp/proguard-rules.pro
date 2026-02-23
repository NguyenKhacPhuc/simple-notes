# SimpleNotes ProGuard Rules

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.simplenotes.app.**$$serializer { *; }
-keepclassmembers class com.simplenotes.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.simplenotes.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Supabase/Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep SQLDelight generated code
-keep class com.simplenotes.app.db.** { *; }

# Keep Koin
-keep class org.koin.** { *; }
