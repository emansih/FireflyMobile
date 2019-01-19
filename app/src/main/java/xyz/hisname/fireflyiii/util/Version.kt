package xyz.hisname.fireflyiii.util


// https://stackoverflow.com/a/11024200
class Version(private val version: String) : Comparable<Version> {

    fun get(): String {
        return this.version
    }

    init {
        if (!version.matches("[0-9]+(\\.[0-9]+)*".toRegex()))
            throw IllegalArgumentException("Invalid version format")
    }

    override operator fun compareTo(that_: Version): Int {
        val thisParts = this.get().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val thatParts = that_.get().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val length = Math.max(thisParts.size, thatParts.size)
        for (i in 0 until length) {
            val thisPart = if (i < thisParts.size)
                Integer.parseInt(thisParts[i])
            else
                0
            val thatPart = if (i < thatParts.size)
                Integer.parseInt(thatParts[i])
            else
                0
            if (thisPart < thatPart)
                return -1
            if (thisPart > thatPart)
                return 1
        }
        return 0
    }

    override fun equals(_that: Any?): Boolean {
        if (this === _that)
            return true
        if (_that == null)
            return false
        return if (this.javaClass != _that.javaClass) false else this.compareTo(_that as Version) == 0
    }

}
