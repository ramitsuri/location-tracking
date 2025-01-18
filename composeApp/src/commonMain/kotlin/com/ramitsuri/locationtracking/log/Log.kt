package com.ramitsuri.locationtracking.log

import co.touchlab.kermit.Logger

fun logD(tag: String, message: () -> String) {
    Logger.d(tag, message = message)
}

fun logI(tag: String, message: () -> String) {
    Logger.i(tag, message = message)
}

fun logW(tag: String, message: () -> String) {
    Logger.w(tag, message = message)
}

fun logE(tag: String, throwable: Throwable? = null, message: () -> String) {
    Logger.e(tag, throwable = throwable, message = message)
}
