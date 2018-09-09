package xyz.hisname.fireflyiii.repository.models.piggy

class PiggyApiResponse() {

    private var error: Throwable? = null
    private var piggyList: PiggyModel? = null
    private var errorMessage: String? = null

    constructor(piggyList: PiggyModel?): this(){
        this.piggyList = piggyList
        this.error = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.piggyList = null
    }

    constructor(errorMessage: String): this(){
        this.errorMessage = errorMessage
        this.piggyList = null
        this.error = null
    }

    fun getPiggy(): PiggyModel? {
        return piggyList
    }

    fun getError(): Throwable? {
        return error
    }

    fun getErrorMessage(): String?{
        return errorMessage
    }
}