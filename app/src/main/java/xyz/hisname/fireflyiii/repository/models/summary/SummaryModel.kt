package xyz.hisname.fireflyiii.repository.models.summary


data class SummaryModel(
        val currency_code: String,
        val currency_decimal_places: Int,
        val currency_id: Int,
        val currency_symbol: String,
        val key: String,
        val local_icon: String,
        val monetary_value: Double,
        val sub_title: String,
        val title: String,
        val value_parsed: String
)