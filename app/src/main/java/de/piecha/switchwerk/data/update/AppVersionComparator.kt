package de.piecha.switchwerk.data.update

object AppVersionComparator {
    fun compare(left: String, right: String): Int {
        val leftParts = parse(left)
        val rightParts = parse(right)
        val maxSize = maxOf(leftParts.size, rightParts.size)

        for (index in 0 until maxSize) {
            val leftPart = leftParts.getOrElse(index) { 0 }
            val rightPart = rightParts.getOrElse(index) { 0 }
            if (leftPart != rightPart) {
                return leftPart.compareTo(rightPart)
            }
        }

        return 0
    }

    fun isNewer(candidate: String, installed: String): Boolean {
        return compare(candidate, installed) > 0
    }

    private fun parse(version: String): List<Int> {
        return version
            .trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")
            .split(".")
            .map { part -> part.toIntOrNull() ?: 0 }
            .dropLastWhile { it == 0 }
            .ifEmpty { listOf(0) }
    }
}
