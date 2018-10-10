package xyz.hisname.fireflyiii.repository.models.auth

class AuthApiResponse() {

    private var error: Throwable? = null
    private var auth: AuthModel? = null


    constructor(authModel: AuthModel?): this(){
        this.auth = authModel
        this.error = null
    }

    constructor(error: Throwable?): this(){
        this.error = error
        this.auth = null
    }


    fun getAuth(): AuthModel? {
        return auth
    }

    fun getError(): Throwable? {
        return error
    }

}