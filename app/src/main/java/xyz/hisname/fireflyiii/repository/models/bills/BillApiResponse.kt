package xyz.hisname.fireflyiii.repository.models.bills


class BillApiResponse() {

    private var error: Throwable? = null
    private var billsList: BillsModel? = null
    private var errorMessage: String? = null

    constructor(billsList: BillsModel?): this(){
        this.billsList = billsList
        this.error = null
        this.errorMessage = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.billsList = null
        this.errorMessage = null
    }

    constructor(errorMessage: String): this(){
        this.errorMessage = errorMessage
        this.billsList = null
        this.error = null
    }

    fun getBill(): BillsModel? {
        return billsList
    }

    fun setBill(billsList: BillsModel?) {
        this.billsList = billsList
    }

    fun getError(): Throwable? {
        return error
    }

    fun setError(error: Throwable) {
        this.error = error
    }

    fun getErrorMessage(): String?{
        return errorMessage
    }

}