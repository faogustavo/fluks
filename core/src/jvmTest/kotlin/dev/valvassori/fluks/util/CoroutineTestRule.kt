package dev.valvassori.fluks.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.coroutines.ContinuationInterceptor

@ExperimentalCoroutinesApi
class CoroutineTestRule : TestWatcher(), TestCoroutineScope by TestCoroutineScope() {
    val testDispatcher = coroutineContext[ContinuationInterceptor] as TestCoroutineDispatcher

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
        super.starting(description)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}