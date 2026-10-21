# R8 keep rules. Most libraries here (Hilt, Room, Firebase, Compose) ship their own consumer
# rules, so this file only covers reflection-based cases they don't.

# Keep generic signatures and annotations (needed by kotlinx.serialization).
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# kotlinx.serialization — type-safe navigation routes and any @Serializable model.
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    *** INSTANCE;
}
-keepclasseswithmembers class **$$serializer {
    <fields>;
}

# Firestore maps these by reflection (toObject / set), so their names and no-arg constructors must survive.
-keep class com.dimitrijeilic.rygg.feature.sync.data.remote.** { *; }

# Crash stack traces map back to source.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
