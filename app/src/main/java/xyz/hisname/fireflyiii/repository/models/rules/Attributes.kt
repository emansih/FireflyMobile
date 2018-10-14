package xyz.hisname.fireflyiii.repository.models.rules

data class Attributes(
        val updated_at: String,
        val created_at: String,
        val title: String,
        val text: Any,
        val order: Int,
        val active: Boolean,
        val stop_processing: Boolean,
        val strict: Boolean
)