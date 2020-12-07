package xyz.hisname.fireflyiii.repository.models

data class ApiResponses<T>(
        val error: Throwable? = null,
        val response: T? = null,
        val errorMessage: String? = null
)