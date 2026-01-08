package org.appjam.smashing.global.extensions

inline fun <reified T : Enum<T>> ofIgnoreCase(
    name: String
): T {
    return enumValues<T>().firstOrNull {
        it.name.equals(name, ignoreCase = true)
    } ?: throw IllegalArgumentException("No enum constant for name: $name")
}

inline fun <reified T : Enum<T>> ofIgnoreCaseOrNull(
    name: String?
): T? {
    return enumValues<T>().firstOrNull {
        it.name.equals(name, ignoreCase = true)
    }
}
