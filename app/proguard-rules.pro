# Preserve the structural integrity of your despair.
# The compiler must not mangle the lexicon, the database schema, or the service bindings.

-keep class com.hereliesaz.reup.DailyDistortion { *; }
-keep class com.hereliesaz.reup.SpiralDatabaseHelper { *; }
-keep class com.hereliesaz.reup.SpiralObserverService { *; }

# Standard Android lifecycle protections.
-keep public class * extends android.app.Service
-keep public class * extends android.app.Activity
-keep public class * extends android.view.View
