package com.ramitsuri.locationtracking.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.ramitsuri.locationtracking.data.dao.LogItemDao
import com.ramitsuri.locationtracking.model.LogItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DbLogWriter(
    private val logItemDao: LogItemDao,
    private val scope: CoroutineScope,
) : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        scope.launch {
            logItemDao.insert(
                LogItem(
                    message = message,
                    tag = tag,
                    errorMessage = throwable?.message,
                    stackTrace = throwable?.stackTraceToString(),
                ),
            )
        }
    }

    suspend fun getAllLogs(tags: List<String>): List<LogItem> {
        return logItemDao.getAll(tags)
    }

    fun getAllTags(): Flow<List<String>> {
        return logItemDao.getTags()
    }

    fun clear() {
        scope.launch {
            logItemDao.deleteAll()
        }
    }
}
