package com.meita.snapshelf.privacy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ManifestPrivacyTest {
    @Test
    fun manifestRemovesNetworkPermissionsAddedByLibraries() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("tools:node=\"remove\""))
        assertFalse(manifest.contains("<uses-permission android:name=\"android.permission.INTERNET\" />"))
    }
}
