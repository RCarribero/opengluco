package com.example.opengluco.auto

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AutoManifestAndSecurityTest {

    @Test
    fun testNetworkSecurityConfigFileExistsAndIsRestrictive() {
        val candidates = listOf(
            File("src/main/res/xml/network_security_config.xml"),
            File("app-auto/src/main/res/xml/network_security_config.xml")
        )
        val file = candidates.firstOrNull { it.exists() }
        assertTrue("network_security_config.xml must exist in app-auto/src/main/res/xml", file != null && file.exists())

        val content = file!!.readText()
        assertTrue("Base config must disallow cleartext traffic", content.contains("cleartextTrafficPermitted=\"false\""))
        assertTrue("Base config must specify trust anchors system certificates", content.contains("<certificates src=\"system\" />"))
        assertTrue("Localhost domain config must allow cleartext for local pairing only", content.contains("<domain includeSubdomains=\"true\">127.0.0.1</domain>"))
        assertTrue("Localhost domain config must allow cleartext for localhost", content.contains("<domain includeSubdomains=\"true\">localhost</domain>"))
    }

    @Test
    fun testAndroidManifestDeclaresNetworkSecurityConfigAndDisallowsBackup() {
        val candidates = listOf(
            File("src/main/AndroidManifest.xml"),
            File("app-auto/src/main/AndroidManifest.xml")
        )
        val file = candidates.firstOrNull { it.exists() }
        assertTrue("AndroidManifest.xml must exist in app-auto/src/main", file != null && file.exists())

        val content = file!!.readText()
        assertTrue("AndroidManifest must disable backup (allowBackup=false)", content.contains("android:allowBackup=\"false\""))
        assertTrue(
            "AndroidManifest must reference networkSecurityConfig",
            content.contains("android:networkSecurityConfig=\"@xml/network_security_config\"")
        )
    }
}
