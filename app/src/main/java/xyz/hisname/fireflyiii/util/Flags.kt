/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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