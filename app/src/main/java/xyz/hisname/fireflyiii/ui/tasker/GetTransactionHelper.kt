package xyz.hisname.fireflyiii.ui.tasker

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput

class GetTransactionHelper(config: TaskerPluginConfig<GetTransactionInput>):
        TaskerPluginConfigHelper<GetTransactionInput, GetTransactionOutput, GetTransactionRunner>(config) {
    override val inputClass = GetTransactionInput::class.java
    override val outputClass = GetTransactionOutput::class.java
    override val runnerClass = GetTransactionRunner::class.java
}