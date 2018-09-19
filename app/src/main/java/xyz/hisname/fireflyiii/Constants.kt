package xyz.hisname.fireflyiii

class Constants private constructor() {

    companion object {
        const val REDIRECT_URI = "http://${BuildConfig.HOSTNAME}"
        const val WEEKLY = "Weekly"
        const val MONTHLY = "Monthly"
        const val QUARTERLY = "Quarterly"
        const val HALF_YEARLY = "Half-yearly"
        const val YEARLY = "Yearly"
        const val OAUTH_API_ENDPOINT = "/oauth"
        const val SYSTEM_INFO_ENDPOINT = "/api/v1/about"
        const val BILL_API_ENDPONT = "/api/v1/bills"
        const val PIGGY_BANK_API_ENDPOINT = "/api/v1/piggy_banks"
        const val RECURRENCE_API_ENDPOINT = "/api/v1/recurrences"
        const val CURRENCY_API_ENDPOINT = "/api/v1/currencies"
        const val TRANSACTION_API_ENDPOINT = "/api/v1/transactions"
        const val ACCOUNTS_API_ENDPOINT = "/api/v1/accounts"
        const val DB_NAME = "firefly.db"
    }
}