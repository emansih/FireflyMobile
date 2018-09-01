package xyz.hisname.fireflyiii.repository.models.transaction

class TransactionApiResponse() {

    private var error: Throwable? = null
    private var transList: TransactionModel? = null

    constructor(transList: TransactionModel?): this(){
        this.transList = transList
        this.error = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.transList = null
    }

    fun getTransaction(): TransactionModel? {
        return transList
    }

    fun setTransaction(billsList: TransactionModel?) {
        this.transList = billsList
    }

    fun getError(): Throwable? {
        return error
    }

    fun setError(error: Throwable) {
        this.error = error
    }

}