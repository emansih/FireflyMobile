package xyz.hisname.fireflyiii.repository.models.transaction

import xyz.hisname.fireflyiii.repository.models.transaction.sucess.TransactionSucessModel

class TransactionApiResponse() {

    private var error: Throwable? = null
    private var transList: TransactionModel? = null
    private var success: TransactionSucessModel? = null
    private var errorMessage: String? = null

    constructor(transList: TransactionModel?): this(){
        this.transList = transList
        this.error = null
        this.errorMessage = null
        this.success = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.transList = null
        this.errorMessage = null
        this.success = null
    }

    constructor(success: TransactionSucessModel?): this(){
        this.transList = null
        this.error = null
        this.errorMessage = null
        this.success = success
    }

    constructor(errorMessage: String): this(){
        this.errorMessage = errorMessage
        this.transList = null
        this.error = null
        this.success = null
    }

    fun getTransaction(): TransactionModel? {
        return transList
    }

    fun getError(): Throwable? {
        return error
    }

    fun getErrorMessage(): String?{
        return errorMessage
    }

    fun getSuccess(): TransactionSucessModel? {
        return success
    }

}