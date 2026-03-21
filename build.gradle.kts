/**
 * The overarching architectural laws. 
 * We declare the plugins here to govern the entire repository, but do not apply them to the root.
 */
plugins {
    id("com.android.application") version "9.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20" apply false
}
