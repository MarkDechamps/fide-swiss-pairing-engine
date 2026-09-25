package io.github.markdechamps.fideswiss.dutch;

/** Dutch 1.7, weakest first so that {@code compareTo} reads as "stronger than". */
enum PreferenceStrength {
    NONE,
    MILD,
    STRONG,
    ABSOLUTE
}
