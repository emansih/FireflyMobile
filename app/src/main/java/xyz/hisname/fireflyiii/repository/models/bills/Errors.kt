package xyz.hisname.fireflyiii.repository.models.bills

data class Errors(
        val name: List<String>?,
        val currency_code: List<String>?,
        val amount_min: List<String>?,
        val repeat_freq: List<String>?,
        val automatch: List<String>?
)