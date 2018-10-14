package xyz.hisname.fireflyiii.repository.models.rules

data class AttributesX(
        val updated_at: String,
        val created_at: String,
        val action_type: String,
        val action_value: String,
        val order: Int,
        val active: Boolean,
        val stop_processing: Boolean
)