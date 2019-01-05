package xyz.hisname.fireflyiii

class Constants private constructor() {

    companion object {
        const val REDIRECT_URI = "http://${BuildConfig.HOSTNAME}"
        const val OAUTH_API_ENDPOINT = "/oauth"
        const val SYSTEM_INFO_ENDPOINT = "/api/v1/about"
        const val BILL_API_ENDPONT = "/api/v1/bills"
        const val PIGGY_BANK_API_ENDPOINT = "/api/v1/piggy_banks"
        const val RECURRENCE_API_ENDPOINT = "/api/v1/recurrences"
        const val CURRENCY_API_ENDPOINT = "/api/v1/currencies"
        const val TRANSACTION_API_ENDPOINT = "/api/v1/transactions"
        const val ACCOUNTS_API_ENDPOINT = "/api/v1/accounts"
        const val SETTINGS_API_ENDPOINT = "api/v1/preferences"
        const val RULES_API_ENDPOINT = "/api/v1/rules"
        const val CATEGORY_API_ENDPOINT = "/api/v1/categories"
        const val BUDGET_API_ENDPOINT = "/api/v1/available_budgets"
        const val TAGS_API_ENDPOINT = "/api/v1/tags"
        const val DB_NAME = "firefly.db"
        const val PIGGY_BANK_CHANNEL = "xyz.hisname.fireflyiii.PIGGY_BANK"
        const val PIGGY_BANK_CHANNEL_DESCRIPTION = "Show Piggy Bank Notifications"
        const val BILL_CHANNEL = "xyz.hisname.fireflyiii.BILL"
        const val BILL_CHANNEL_DESCRIPTION = "Show Bill Notifications"
        const val TRANSACTION_CHANNEL = "xyz.hisname.fireflyiii.TRANSACTION"
        const val TRANSACTION_CHANNEL_DESCRIPTION = "Show Transaction Notifications"
        const val ACCOUNT_CHANNEL = "xyz.hisname.fireflyiii.ACCOUNT"
        const val ACCOUNT_CHANNEL_DESCRIPTION = "Show Account Notifications"
        const val GENERAL_NOTIFICATION = "xyz.hisname.fireflyiii.GENERAL"
        const val PROFILE_URL = "https://images.unsplash.com/photo-1531987428847-95ad50737a07?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&w=1001&q=80"
    }
}