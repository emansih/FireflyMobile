package xyz.hisname.fireflyiii.data.remote.nominatim

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import xyz.hisname.fireflyiii.repository.models.Element
import java.lang.reflect.Type

class NominatimDeserializer: JsonDeserializer<Array<Element>> {

    override fun deserialize(json: JsonElement, typeOfT: Type,
                    context: JsonDeserializationContext): Array<Element> {
        var elements: Array<Element> = arrayOf()
        if (json.isJsonObject) {
            elements = arrayOf(json.asJsonObject.entrySet().size) as Array<Element>
            for ((i, elem) in json.asJsonObject.entrySet().withIndex()) {
                elements[i] = Element()
                elements[i].value = elem.key
                elements[i].value = elem.value.asString
            }
        }
        return elements
    }
}