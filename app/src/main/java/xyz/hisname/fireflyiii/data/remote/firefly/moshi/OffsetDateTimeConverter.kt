package xyz.hisname.fireflyiii.data.remote.firefly.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDateTimeConverter: JsonAdapter<OffsetDateTime>() {

    companion object {
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    override fun fromJson(reader: JsonReader): OffsetDateTime {
        return FORMATTER.parse(reader.nextString(), OffsetDateTime::from)
    }

    override fun toJson(writer: JsonWriter, value: OffsetDateTime?) {
    }

}