package xyz.hisname.fireflyiii.util.extension

import java.nio.charset.StandardCharsets


//https://stackoverflow.com/a/3585247

fun String.isAscii(): Boolean{
    return StandardCharsets.US_ASCII.newEncoder().canEncode(this)
}