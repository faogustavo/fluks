package dev.valvassori.fluks.middlewares

import dev.valvassori.fluks.Fluks
import dev.valvassori.fluks.Next
import dev.valvassori.fluks.ext.value
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO: Migrate to `commonTest` module after the fix from this bug https://github.com/mockk/mockk/issues/322
@ExperimentalCoroutinesApi
class LogMiddlewareTest {

    data class State(val updated: Boolean) : Fluks.State

    object TestAction : Fluks.Action

    private val store = mockk<Fluks.Store<State>>()
    private val logger = mockk<Logger>()
    private val next = mockk<Next<State>>()

    private val subject by lazy { logMiddleware<State>(logger) }

    @Test
    fun execute_returnsNextResult() {
        mock()

        val result = subject.execute(store, TestAction, next)

        val expectedResult = State(true)
        assertEquals(expectedResult, result)
    }

    @Test
    fun execute_printsOldText() {
        mock()

        subject.execute(store, TestAction, next)

        val expectedOldValue = State(false)
        verify(exactly = 1) { logger.log("[Old State]: $expectedOldValue") }
    }

    @Test
    fun execute_printsNewText() {
        mock()

        subject.execute(store, TestAction, next)

        val expectedNewValue = State(true)
        verify(exactly = 1) { logger.log("[New State]: $expectedNewValue") }
    }

    @Test
    fun execute_printsAction() {
        mock()

        subject.execute(store, TestAction, next)

        val expectedAction = TestAction::class.simpleName
        verify(exactly = 1) { logger.log("[Action]: $expectedAction") }
    }

    private fun mock() {
        mockkStatic("dev.valvassori.fluks.ext.StoreExtKt")
        every { store.value } returns State(false)
        every { next.invoke(any()) } returns State(true)
        every { logger.log(any()) } just Runs
    }
}
