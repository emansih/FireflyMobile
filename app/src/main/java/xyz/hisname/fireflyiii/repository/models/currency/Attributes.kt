package xyz.hisname.fireflyiii.repository.models.currency

data class Attributes(
        val updated_at: String,
        val created_at: String,
        val name: String,
        val code: String,
        val symbol: String,
        val decimal_places: Int,
        val default: Boolean
)