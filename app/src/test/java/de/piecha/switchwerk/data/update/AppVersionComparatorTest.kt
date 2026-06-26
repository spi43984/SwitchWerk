package de.piecha.switchwerk.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionComparatorTest {
    @Test
    fun semanticVersionsAreComparedNumerically() {
        assertTrue(AppVersionComparator.isNewer("0.7.1", "0.7.0"))
        assertTrue(AppVersionComparator.isNewer("0.10.0", "0.9.9"))
        assertFalse(AppVersionComparator.isNewer("0.7.0", "0.7.0"))
        assertFalse(AppVersionComparator.isNewer("0.7.0", "0.7.1"))
    }

    @Test
    fun versionPrefixAndSuffixAreIgnored() {
        assertTrue(AppVersionComparator.isNewer("v1.0.0", "0.9.9"))
        assertFalse(AppVersionComparator.isNewer("1.0.0-beta.1", "1.0.0"))
    }
}
