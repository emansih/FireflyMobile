package xyz.hisname.fireflyiii.repository.models.accounts

class AccountApiResponse(){

    private var error: Throwable? = null
    private var accountList: AccountsModel? = null
    private var errorMessage: String? = null

    constructor(billsList: AccountsModel?): this(){
        this.accountList = billsList
        this.error = null
        this.errorMessage = null
    }

    constructor(error: Throwable?): this(){
        this.error = error
        this.accountList = null
        this.errorMessage = null
    }

    constructor(errorMessage: String?): this(){
        this.errorMessage = errorMessage
        this.accountList = null
        this.error = null
    }


    fun getAccounts(): AccountsModel? {
        return accountList
    }

    fun getError(): Throwable? {
        return error
    }

    fun getErrorMessage(): String?{
        return errorMessage
    }

}