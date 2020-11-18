package xyz.hisname.fireflyiii

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import xyz.hisname.fireflyiii.util.Version

class VersionTestCase {

    @Test
    fun isVersionLower(){
        val versionCode = Version("1.1.1").compareTo(Version("1.1.2"))
        assertEquals(-1, versionCode)
    }

    @Test
    fun isVersionEquals(){
        val isEquals = Version("1.1.1") == Version("1.1.1")
        assertEquals(true, isEquals)
    }

    @Test
    fun isVersionHigher(){
        val versionCode = Version("1.1.2").compareTo(Version("1.1.1"))
        assertEquals(1, versionCode)
    }

    @Test
    fun isVersionNotEquals(){
        val isEquals = Version("1.1.1") == Version("1.1.0")
        assertEquals(false, isEquals)
    }

    @Test
    fun isVersionWithDecimalEquals(){
        val isEquals = Version("1.0") == Version("1")
        assertEquals(true, isEquals)
    }
}