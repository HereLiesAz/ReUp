/**
 * The overarching architectural laws. 
 * We declare the plugins here to govern the entire repository, but do not apply them to the root.
 */
plugins {
    id("com.android.application") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
}
