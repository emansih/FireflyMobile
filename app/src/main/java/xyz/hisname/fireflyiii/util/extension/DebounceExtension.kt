package xyz.hisname.fireflyiii.util.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun <T> debounce(coroutineContext: CoroutineContext,
                 f: (T) -> Unit): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        if (debounceJob?.isCompleted != false) {
            debounceJob = CoroutineScope(coroutineContext).launch {
                delay(2500L)
                f(param)
            }
        }
    }
}