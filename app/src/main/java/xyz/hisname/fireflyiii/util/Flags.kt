package xyz.hisname.fireflyiii.util

class Flags{

    companion object {

        private const val COMMIT_HASH = "c7ad12c5678f71d2e2895b1bb02fa8fab80aaca1"
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