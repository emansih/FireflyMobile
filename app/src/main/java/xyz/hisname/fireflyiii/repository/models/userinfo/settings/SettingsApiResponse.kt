package xyz.hisname.fireflyiii.repository.models.userinfo.settings

class SettingsApiResponse (){
    private var error: Throwable? = null
    private var settings: SettingsModel? = null

    constructor(settings: SettingsModel?): this(){
        this.settings = settings
        this.error = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.settings = null
    }

    fun getUserSettings(): SettingsModel? {
        return settings
    }

    fun getError(): Throwable? {
        return error
    }

}