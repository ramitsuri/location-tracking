package com.ramitsuri.locationtracking.testutils

import org.junit.Rule
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule

open class BaseTest : KoinTest {
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(testModule)
    }
}
