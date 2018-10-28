package xyz.hisname.fireflyiii.repository.models

class ApiResponses<T>(){

    private var error: Throwable? = null
    private var apiResponse: T? = null
    private var errorMessage: String? = null

    constructor(apiResponse: T?): this(){
        this.apiResponse = apiResponse
        this.error = null
        this.errorMessage = null
    }

    constructor(error: Throwable?): this(){
        this.error = error
        this.apiResponse = null
        this.errorMessage = null
    }

    constructor(errorMessage: String?): this(){
        this.errorMessage = errorMessage
        this.apiResponse = null
        this.error = null
    }

    fun getResponse(): T? {
        return apiResponse
    }

    fun getError(): Throwable? {
        return error
    }

    fun getErrorMessage(): String?{
        return errorMessage
    }
}