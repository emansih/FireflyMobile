package xyz.hisname.fireflyiii.repository.models.rules

class RulesApiResponse() {

    private var error: Throwable? = null
    private var rulesList: Rulesmodel? = null
    private var errorMessage: String? = null

    constructor(rulesList: Rulesmodel?): this(){
        this.rulesList = rulesList
        this.error = null
        this.errorMessage = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.rulesList = null
        this.errorMessage = null
    }

    constructor(errorMessage: String): this(){
        this.errorMessage = errorMessage
        this.rulesList = null
        this.error = null
    }

    fun getRules(): Rulesmodel? {
        return rulesList
    }

    fun getError(): Throwable? {
        return error
    }

    fun getErrorMessage(): String?{
        return errorMessage
    }

}