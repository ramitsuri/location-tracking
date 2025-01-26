package com.ramitsuri.locationtracking.testutils

import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import org.junit.After
import org.junit.Rule
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule

open class BaseTest : KoinTest {
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(testModule)
    }

    @OptIn(ExperimentalPathApi::class)
    @After
    fun tearDown() {
        stopKoin()
        Paths.get(TEMP_DIR).deleteRecursively()
    }

    companion object {
        const val TEMP_DIR = "temp"
    }
}
