package com.ramitsuri.locationtracking.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.ramitsuri.locationtracking.data.dao.LogItemDao
import com.ramitsuri.locationtracking.model.LogItem
import com.ramitsuri.locationtracking.model.toLogLevel
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class DbLogWriter(
    scope: CoroutineScope,
    private val logItemDao: LogItemDao,
    private val clock: Clock = Clock.System,
) : LogWriter() {
    private val logItems = MutableStateFlow<List<LogItem>>(listOf())
    private var lastWriteTime = Instant.DISTANT_PAST

    init {
        scope.launch {
            logItems.collect { logs ->
                if (logs.size >= 10 ||
                    clock.now().minus(lastWriteTime) >= SAVE_INTERVAL_MINUTES.minutes
                ) {
                    logItemDao.insert(logs)
                    lastWriteTime = clock.now()
                    logItems.update { it.minus(logs.toSet()) }
                    logD(TAG) { "wrote ${logs.size} logs to db" }
                }
            }
        }
    }

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        logItems.update {
            it.plus(
                LogItem(
                    message = message,
                    tag = tag,
                    errorMessage = throwable?.message,
                    stackTrace = throwable?.stackTraceToString(),
                    level = severity.toLogLevel(),
                ),
            )
        }
    }

    companion object {
        private const val TAG = "DbLogWriter"
        private const val SAVE_INTERVAL_MINUTES = 10
    }
}
