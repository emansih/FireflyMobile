package xyz.hisname.fireflyiii.repository.models.userinfo.user

class UserInfoApiResponse(){
    private var error: Throwable? = null
    private var userDataModel: UserDataModel? = null

    constructor(userDataModel: UserDataModel?): this(){
        this.userDataModel = userDataModel
        this.error = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.userDataModel = null
    }

    fun getUserData(): UserDataModel? {
        return userDataModel
    }

    fun getError(): Throwable? {
        return error
    }

}