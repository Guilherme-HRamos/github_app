package br.com.gabrielmarcos.githubmvvm.util


fun printer(value: String) {
    println("Gui - $value")
}

fun emptyString(): String {
    return ""
}

inline fun <R> Boolean.then(block: () -> R): R? {
    if (this) return block()
    return null
}