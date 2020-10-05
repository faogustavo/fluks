package dev.valvassori.fluks

import dev.valvassori.fluks.ext.value
import dev.valvassori.fluks.util.CoroutineTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class GlobalDispatcherTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private data class TestState(val switch: Boolean) : Fluks.State
    private object Switch : Fluks.Action

    private val initialValue = TestState(false)
    private val reducer = reducer<TestState> { state, action ->
        state.copy(switch = state.switch.not())
    }

    @Test
    fun dispatch_callsAllStores() = coroutineTestRule.runBlockingTest {
        val store1 = store(initialValue = initialValue, context = Dispatchers.Main, reducer = reducer)
        val store2 = store(initialValue = initialValue, context = Dispatchers.Main, reducer = reducer)
        val store3 = store(initialValue = initialValue, context = Dispatchers.Main, reducer = reducer)

        dispatch(Switch)

        assertTrue(store1.value.switch)
        assertTrue(store2.value.switch)
        assertTrue(store3.value.switch)

        dispatch(Switch)

        assertFalse(store1.value.switch)
        assertFalse(store2.value.switch)
        assertFalse(store3.value.switch)
    }
}