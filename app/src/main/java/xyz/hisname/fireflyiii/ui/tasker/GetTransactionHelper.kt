package xyz.hisname.fireflyiii.ui.tasker

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput

class GetTransactionHelper(config: TaskerPluginConfig<GetTransactionInput>):
        TaskerPluginConfigHelper<GetTransactionInput, GetTransactionOutput, GetTransactionRunner>(config) {
    override val inputClass = GetTransactionInput::class.java
    override val outputClass = GetTransactionOutput::class.java
    override val runnerClass = GetTransactionRunner::class.java

    override fun addToStringBlurb(input: TaskerInput<GetTransactionInput>, blurbBuilder: StringBuilder) {
        super.addToStringBlurb(input, blurbBuilder)
        blurbBuilder.append("\n\nNote: Most of the time you will only need to read from %response")
    }
}