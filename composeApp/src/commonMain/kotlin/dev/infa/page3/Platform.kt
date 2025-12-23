package dev.infa.page3

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform