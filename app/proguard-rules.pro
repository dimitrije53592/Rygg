# R8 keep rules. Most libraries here (Hilt, Room, Firebase, Compose) ship their own consumer
# rules, so this file only covers reflection-based cases the template uses or commonly needs.

# Keep generic signatures and annotations (needed by Gson, serialization, retrofit).
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# kotlinx.serialization — type-safe navigation routes and any @Serializable model.
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    *** INSTANCE;
}
-keepclasseswithmembers class **$$serializer {
    <fields>;
}

# Gson — model classes are mapped reflectively. Replace the package below with your API models
# (or annotate every field with @SerializedName) so R8 does not rename their fields.
# -keep class com.example.template.**.data.api.** { *; }

# Crash stack traces map back to source.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
