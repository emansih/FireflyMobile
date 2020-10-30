package xyz.hisname.fireflyiii.repository.models

import com.squareup.moshi.JsonClass
import org.json.JSONObject

// https://stackoverflow.com/questions/59066476/how-do-i-use-moshi-to-serialize-a-json-string-into-org-json-jsonobject
@JsonClass(generateAdapter = true)
data class SummaryModel(
        val someProperty: String,
        val someDynamicProperty: JSONObject?
)
