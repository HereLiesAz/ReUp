/**
 * The overarching architectural laws. 
 * We declare the plugins here to govern the entire repository, but do not apply them to the root.
 */
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}
