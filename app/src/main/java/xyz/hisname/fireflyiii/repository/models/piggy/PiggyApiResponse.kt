package xyz.hisname.fireflyiii.repository.models.piggy

import xyz.hisname.fireflyiii.repository.models.piggy.success.PiggySuccessModel

class PiggyApiResponse() {

    private var error: Throwable? = null
    private var piggyList: PiggyModel? = null
    private var errorMessage: String? = null
    private var success: PiggySuccessModel? = null

    constructor(piggyList: PiggyModel?): this(){
        this.piggyList = piggyList
        this.error = null
        this.errorMessage = null
        this.success = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.piggyList = null
        this.errorMessage = null
        this.success = null
    }

    constructor(errorMessage: String): this(){
        this.errorMessage = errorMessage
        this.piggyList = null
        this.error = null
        this.success = null
    }

    constructor(success: PiggySuccessModel?): this(){
        this.piggyList = null
        this.error = null
        this.errorMessage = null
        this.success = success
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

    fun getSuccess(): PiggySuccessModel? {
        return success
    }
}