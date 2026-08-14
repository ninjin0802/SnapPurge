package com.meita.snapshelf.privacy

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ManifestPrivacyTest {
    @Test
    fun manifestAllowsOnlyUserInitiatedUpdateNetworkAccess() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("<uses-permission android:name=\"android.permission.INTERNET\" />"))
        assertTrue(manifest.contains("android.permission.ACCESS_NETWORK_STATE"))
        assertTrue(manifest.contains("tools:node=\"remove\""))
    }
}
