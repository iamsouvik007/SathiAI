package com.example.sathiai.services

object ScreenAnalyzer {

    fun cleanText(raw: String): String {
        return raw
            .lines()
            .map { it.trim() }
            .filter { line ->
                line.isNotBlank() &&
                        line.length > 2 &&
                        !line.startsWith("http") &&
                        !line.matches(Regex("\\d+"))
            }
            .distinct()
            .take(80)
            .joinToString("\n")
    }
}