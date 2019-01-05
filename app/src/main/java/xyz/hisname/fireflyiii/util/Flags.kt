package xyz.hisname.fireflyiii.util

class Flags{

    companion object {

        private const val COMMIT_HASH = "48e8b89a695bf375fce170c52bb9300d1d6bd2e5"
        private const val GITHUB_URL =
                "https://raw.githubusercontent.com/transferwise/currency-flags/$COMMIT_HASH/src/flags/"

        fun getFlagByIso(isoName: String): String{
            return when(isoName.toLowerCase()){
                "rmb" -> GITHUB_URL + "cny.png"
                "bch" -> "file:///android_asset/flags/bch.png"
                "xbt" -> "file:///android_asset/flags/bch.png"
                "eth" -> "file:///android_asset/flags/eth.png"
                else -> GITHUB_URL + isoName.toLowerCase() + ".png"
            }
        }
    }
}