package xyz.hisname.fireflyiii.repository.models.userinfo.system

data class SystemData(
        val version: String,
        val api_version: String,
        val php_version: String,
        val os: String,
        val driver: String
)