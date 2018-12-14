package xyz.hisname.fireflyiii.util

import android.content.res.AssetManager
import android.graphics.drawable.Drawable
import java.io.*

class Flags{

    companion object {
        fun getCurrencyFlagsByIso(assetManager: AssetManager, isoName: String): Drawable?{
            var inputStream: InputStream? = null
            val drawable: Drawable?
            try {
                inputStream = when(isoName.toUpperCase()){
                    "BCH" -> assetManager.open("flags/bch.webp")
                    "EUR" -> assetManager.open("flags/eur.webp")
                    "USD" -> assetManager.open("flags/usd.webp")
                    "GBP" -> assetManager.open("flags/gbp.webp")
                    "CZK" -> assetManager.open("flags/czk.webp")
                    "TRY" -> assetManager.open("flags/try.webp")
                    "AED" -> assetManager.open("flags/aed.webp")
                    "AFN" -> assetManager.open("flags/afn.webp")
                    "ARS" -> assetManager.open("flags/ars.webp")
                    "AUD" -> assetManager.open("flags/aud.webp")
                    "BBD" -> assetManager.open("flags/bbd.webp")
                    "BDT" -> assetManager.open("flags/bdt.webp")
                    "BGN" -> assetManager.open("flags/bgn.webp")
                    "BHD" -> assetManager.open("flags/bhd.webp")
                    "BMD" -> assetManager.open("flags/bmd.webp")
                    "BND" -> assetManager.open("flags/bnd.webp")
                    "BOB" -> assetManager.open("flags/bob.webp")
                    "BRL" -> assetManager.open("flags/brl.webp")
                    "BTN" -> assetManager.open("flags/btn.webp")
                    "BZD" -> assetManager.open("flags/bzd.webp")
                    "CAD" -> assetManager.open("flags/cad.webp")
                    "CHF" -> assetManager.open("flags/chf.webp")
                    "CLP" -> assetManager.open("flags/clp.webp")
                    "COP" -> assetManager.open("flags/cop.webp")
                    "CRC" -> assetManager.open("flags/crc.webp")
                    "DKK" -> assetManager.open("flags/dkk.webp")
                    "DOP" -> assetManager.open("flags/dop.webp")
                    "EGP" -> assetManager.open("flags/egp.webp")
                    "ETB" -> assetManager.open("flags/etb.webp")
                    "GEL" -> assetManager.open("flags/gel.webp")
                    "GHS" -> assetManager.open("flags/ghs.webp")
                    // gmd.png not converted
                    "GMD" -> assetManager.open("flags/gmd.png")
                    "GYD" -> assetManager.open("flags/gyd.webp")
                    "HKD" -> assetManager.open("flags/hkd.webp")
                    "HRK" -> assetManager.open("flags/hrk.webp")
                    "HUF" -> assetManager.open("flags/huf.webp")
                    "IDR" -> assetManager.open("flags/idr.webp")
                    "ILS" -> assetManager.open("flags/ils.webp")
                    "INR" -> assetManager.open("flags/inr.webp")
                    // isk.png not converted
                    "ISK" -> assetManager.open("flags/isk.png")
                    "JMD" -> assetManager.open("flags/jmd.webp")
                    "JPY" -> assetManager.open("flags/jpy.webp")
                    "KES" -> assetManager.open("flags/kes.webp")
                    "KRW" -> assetManager.open("flags/krw.webp")
                    "KWD" -> assetManager.open("flags/kwd.webp")
                    "KYD" -> assetManager.open("flags/kyd.webp")
                    "KZT" -> assetManager.open("flags/kzt.webp")
                    "LAK" -> assetManager.open("flags/lak.webp")
                    "LKR" -> assetManager.open("flags/lkr.webp")
                    "LRD" -> assetManager.open("flags/lrd.webp")
                    "LTL" -> assetManager.open("flags/ltl.webp")
                    "MAD" -> assetManager.open("flags/mad.webp")
                    "MDL" -> assetManager.open("flags/mdl.webp")
                    "MKD" -> assetManager.open("flags/mkd.webp")
                    "MNT" -> assetManager.open("flags/mnt.webp")
                    "MUR" -> assetManager.open("flags/mur.webp")
                    "MWK" -> assetManager.open("flags/mwk.webp")
                    "MXN" -> assetManager.open("flags/mxn.webp")
                    "MYR" -> assetManager.open("flags/myr.webp")
                    "MZN" -> assetManager.open("flags/mzn.webp")
                    "NAD" -> assetManager.open("flags/nad.webp")
                    "NGN" -> assetManager.open("flags/ngn.webp")
                    "NIO" -> assetManager.open("flags/nio.webp")
                    "NOK" -> assetManager.open("flags/nok.webp")
                    "NPR" -> assetManager.open("flags/npr.webp")
                    "NZD" -> assetManager.open("flags/nzd.webp")
                    "OMR" -> assetManager.open("flags/omr.webp")
                    "PEN" -> assetManager.open("flags/pen.webp")
                    "PGK" -> assetManager.open("flags/pgk.webp")
                    "PHP" -> assetManager.open("flags/php.webp")
                    "PKR" -> assetManager.open("flags/pkr.webp")
                    "PLN" -> assetManager.open("flags/pln.webp")
                    "PYG" -> assetManager.open("flags/pyg.webp")
                    "QAR" -> assetManager.open("flags/qar.webp")
                    "RMB" -> assetManager.open("flags/cny.webp")
                    "RON" -> assetManager.open("flags/ron.webp")
                    "RSD" -> assetManager.open("flags/rsd.webp")
                    "RUB" -> assetManager.open("flags/rub.webp")
                    "SAR" -> assetManager.open("flags/sar.webp")
                    "SEK" -> assetManager.open("flags/sek.webp")
                    "SGD" -> assetManager.open("flags/sgd.webp")
                    "SOS" -> assetManager.open("flags/sos.webp")
                    "SRD" -> assetManager.open("flags/srd.webp")
                    "THB" -> assetManager.open("flags/THB.webp")
                    "TTD" -> assetManager.open("flags/ttd.webp")
                    "TWD" -> assetManager.open("flags/twd.webp")
                    "TZS" -> assetManager.open("flags/tzs.webp")
                    "UAH" -> assetManager.open("flags/uah.webp")
                    "UGX" -> assetManager.open("flags/ugx.webp")
                    "UYU" -> assetManager.open("flags/uyu.webp")
                    "VEF" -> assetManager.open("flags/vef.webp")
                    "VND" -> assetManager.open("flags/vnd.webp")
                    "XBT" -> assetManager.open("flags/bch.webp")
                    "YER" -> assetManager.open("flags/yer.webp")
                    "ZAR" -> assetManager.open("flags/zar.webp")
                    else -> assetManager.open("flags/unknown.webp")
                }
                drawable = Drawable.createFromStream(inputStream, null)
            } catch (exception: IOException){
                throw IllegalStateException("Flags missing from assets. Aborting!", exception)
            } finally {
                inputStream?.close()
            }
            return drawable
        }
    }
}