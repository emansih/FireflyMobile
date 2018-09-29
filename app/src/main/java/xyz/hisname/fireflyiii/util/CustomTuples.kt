package xyz.hisname.fireflyiii.util

interface Serializable

// Yes, there's such a thing...
data class Sixple<out A, out B, out C, out D, out E, out F>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E,
        val sixth: F
) : Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth)"
}

