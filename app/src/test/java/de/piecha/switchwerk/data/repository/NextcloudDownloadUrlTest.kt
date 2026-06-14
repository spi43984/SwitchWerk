package de.piecha.switchwerk.data.repository

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NextcloudDownloadUrlTest {

    @Test
    fun publicShareUrlGetsDownloadPath() {
        val url = "https://example.com/nextcloud/s/share-token".toHttpUrl()

        assertEquals(
            "https://example.com/nextcloud/s/share-token/download",
            nextcloudDownloadUrl(url).toString()
        )
    }

    @Test
    fun directJsonUrlIsNotChanged() {
        val url = "https://example.com/config/switchwerk.json".toHttpUrl()

        assertNull(nextcloudDownloadUrl(url))
    }

    @Test
    fun googleDriveViewUrlGetsDirectDownloadUrl() {
        val url = "https://drive.google.com/file/d/file-id/view?usp=drive_link".toHttpUrl()

        assertEquals(
            "https://drive.usercontent.google.com/download?id=file-id&export=download&confirm=t",
            googleDriveDownloadUrl(url).toString()
        )
    }

    @Test
    fun unrelatedGoogleUrlIsNotChanged() {
        val url = "https://docs.google.com/document/d/document-id/edit".toHttpUrl()

        assertNull(googleDriveDownloadUrl(url))
    }
}
