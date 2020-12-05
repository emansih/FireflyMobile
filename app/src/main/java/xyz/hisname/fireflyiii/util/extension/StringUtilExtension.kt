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