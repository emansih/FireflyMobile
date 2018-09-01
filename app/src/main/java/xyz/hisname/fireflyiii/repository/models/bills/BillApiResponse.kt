package xyz.hisname.fireflyiii.repository.models.bills


class BillApiResponse() {

    private var error: Throwable? = null
    private var billsList: BillsModel? = null

    constructor(billsList: BillsModel?): this(){
        this.billsList = billsList
        this.error = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.billsList = null
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

}