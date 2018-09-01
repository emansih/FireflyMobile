package xyz.hisname.fireflyiii

class Constants private constructor() {

    companion object {
        const val REDIRECT_URI = "http://${BuildConfig.HOSTNAME}"
        const val WEEKLY = "weekly"
        const val MONTHLY = "monthly"
        const val QUARTERLY = "quarterly"
        const val HALF_YEARLY = "half-yearly"
        const val YEARLY = "yearly"
        const val OAUTH_API_ENDPOINT = "/oauth"
        const val SYSTEM_INFO_ENDPOINT = "/api/v1/about"
        const val BILL_API_ENDPONT = "/api/v1/bills"
        const val PIGGY_BANK_API_ENDPOINT = "/api/v1/piggy_banks"
        const val RECURRENCE_API_ENDPOINT = "/api/v1/recurrences"
        const val CURRENCY_API_ENDPOINT = "/api/v1/currencies"
        const val TRANSACTION_API_ENDPOINT = "/api/v1/transactions"
        const val DB_NAME = "firefly.db"
    }
}