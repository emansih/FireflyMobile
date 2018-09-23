package xyz.hisname.fireflyiii.repository.models.userinfo.system

class SystemApiResponse(){
    private var error: Throwable? = null
    private var systemInfo: SystemInfoModel? = null

    constructor(systemInfoModel: SystemInfoModel?): this(){
        this.systemInfo = systemInfoModel
        this.error = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.systemInfo = null
    }

    fun getUserSystem(): SystemInfoModel? {
        return systemInfo
    }

    fun getError(): Throwable? {
        return error
    }

}