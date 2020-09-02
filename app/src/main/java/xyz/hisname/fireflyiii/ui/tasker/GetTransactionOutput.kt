package xyz.hisname.fireflyiii.ui.tasker

import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import xyz.hisname.fireflyiii.R

@TaskerOutputObject
class GetTransactionOutput(@get:TaskerOutputVariable("response", R.string.details,
        R.string.tasker_output_description) var response: String?) {
}