package xyz.hisname.fireflyiii.repository.dao

import android.os.Handler
import android.os.HandlerThread

class DbWorker(threadName: String): HandlerThread(threadName) {

    private lateinit var workerHandler: Handler

    override fun onLooperPrepared(){
        super.onLooperPrepared()
        workerHandler = Handler(looper)
    }

    fun postTask(task: Runnable){
        workerHandler.post(task)
    }
}