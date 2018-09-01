package xyz.hisname.fireflyiii.repository.models.piggy

class PiggyApiResponse() {

    private var error: Throwable? = null
    private var piggyList: PiggyModel? = null

    constructor(piggyList: PiggyModel?): this(){
        this.piggyList = piggyList
        this.error = null
    }

    constructor(error: Throwable): this(){
        this.error = error
        this.piggyList = null
    }

    fun getPiggy(): PiggyModel? {
        return piggyList
    }

    fun setPiggy(piggyList: PiggyModel?) {
        this.piggyList = piggyList
    }

    fun getError(): Throwable? {
        return error
    }

    fun setError(error: Throwable) {
        this.error = error
    }

}