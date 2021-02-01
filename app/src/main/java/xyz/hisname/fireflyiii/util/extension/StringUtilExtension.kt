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

package xyz.hisname.fireflyiii.util.extension

import android.os.Build
import android.text.Html
import android.text.Spanned
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.nio.charset.StandardCharsets


//https://stackoverflow.com/a/3585247

fun String.isAscii(): Boolean{
    return StandardCharsets.US_ASCII.newEncoder().canEncode(this)
}

fun String.toMarkDown(): Spanned {
    val extensions = arrayListOf(StrikethroughExtension.create(), AutolinkExtension.create())
    val parser = Parser.builder().extensions(extensions).build()
    val renderer = HtmlRenderer.builder().extensions(extensions).build()
    val document = parser.parse(this)
    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(renderer.render(document), Html.FROM_HTML_MODE_COMPACT)
    } else {
        return Html.fromHtml(renderer.render(document))
    }
}