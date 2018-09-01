package xyz.hisname.fireflyiii.repository.models.userinfo

data class Attributes(
        val updated_at: String,
        val created_at: String,
        val email: String,
        val blocked: Boolean,
        val blocked_code: Any,
        val role: String
)