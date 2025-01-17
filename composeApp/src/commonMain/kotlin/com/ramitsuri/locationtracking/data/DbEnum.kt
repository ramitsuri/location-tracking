package com.ramitsuri.locationtracking.data

import kotlin.enums.enumEntries

interface DbEnum {
    val value: String
}

inline fun <reified T> toEnum(value: String, default: T): T where T : Enum<T>, T : DbEnum =
    enumEntries<T>()
        .firstOrNull { it.value == value }
        ?: default
