package xyz.hisname.fireflyiii.repository.models.currency

class CurrencyApiResponse() {

    private var error: Throwable? = null
    private var currencyList: CurrencyModel? = null
    private var errorMessage: String? = null

    constructor(currencyList: CurrencyModel?): this(){
        this.currencyList = currencyList
        this.error = null
        this.errorMessage = null
    }

    constructor(error: Throwable?): this(){
        this.error = error
        this.currencyList = null
        this.errorMessage = null
    }

    constructor(errorMessage: String?): this(){
        this.errorMessage = errorMessage
        this.currencyList = null
        this.error = null
    }

    fun getCurrency(): CurrencyModel? {
        return currencyList
    }

    fun getError(): Throwable? {
        return error
    }

    fun getErrorMessage(): String?{
        return errorMessage
    }

}