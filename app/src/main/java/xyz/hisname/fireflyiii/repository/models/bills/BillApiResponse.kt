package xyz.hisname.fireflyiii.repository.models.bills

import xyz.hisname.fireflyiii.repository.models.bills.success.BillSucessModel


class BillApiResponse() {

    private var error: Throwable? = null
    private var billsList: BillsModel? = null
    private var errorMessage: String? = null
    private var successModel: BillSucessModel? = null

    constructor(billsList: BillsModel?): this(){
        this.billsList = billsList
        this.error = null
        this.errorMessage = null
        this.successModel = null
    }

    constructor(error: Throwable?): this(){
        this.error = error
        this.billsList = null
        this.errorMessage = null
        this.successModel = null
    }

    constructor(errorMessage: String?): this(){
        this.errorMessage = errorMessage
        this.billsList = null
        this.error = null
        this.successModel = null
    }

    constructor(successModel: BillSucessModel?): this(){
        this.errorMessage = null
        this.billsList = null
        this.error = null
        this.successModel = successModel
    }

    fun getBill(): BillsModel? {
        return billsList
    }

    fun getError(): Throwable? {
        return error
    }

    fun getErrorMessage(): String?{
        return errorMessage
    }

    fun getSuccess(): BillSucessModel? {
        return successModel
    }

}