package xyz.hisname.fireflyiii.data.remote.firefly.moshi

import android.net.Uri
import androidx.core.net.toUri
import com.squareup.moshi.*

class UriConverter: JsonAdapter<Uri>() {
    override fun fromJson(reader: JsonReader): Uri {
        return reader.nextString().toUri()
    }

    override fun toJson(writer: JsonWriter, value: Uri?) { }


}