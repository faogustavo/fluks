package dev.valvassori.fluks

import dev.valvassori.fluks.ext.value
import dev.valvassori.fluks.util.CoroutineTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class AbstractCombinedStoreTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    object Inc : Fluks.Action

    private data class State0(val count0: Int) : Fluks.State
    private data class State1(val count1: Int) : Fluks.State
    private data class StateOut(val multiplication: Int) : Fluks.State

    private val store0 by lazy {
        store(
            initialValue = State0(count0 = 1),
            context = Dispatchers.Main
        ) { currentState, action ->
            when (action) {
                Inc -> currentState.copy(count0 = currentState.count0 + 1)
                else -> currentState
            }
        }
    }

    private val store1 by lazy {
        store(
            initialValue = State1(count1 = 1),
            context = Dispatchers.Main
        ) { currentState, action ->
            when (action) {
                Inc -> currentState.copy(count1 = currentState.count1 + 1)
                else -> currentState
            }
        }
    }

    @Test
    fun combinedStores_shouldRespondCorrectly() = coroutineTestRule.runBlockingTest {
        val combinedStores = combineStores(
            initialValue = StateOut(multiplication = 1),
            store0 = store0,
            store1 = store1,
            baseContext = Dispatchers.Main
        ) { s0, s1 -> StateOut(multiplication = s0.count0 * s1.count1) }

        // 1 - 1
        assertEquals(1, combinedStores.value.multiplication)

        // 2 - 1
        store0.dispatch(Inc)
        assertEquals(2, combinedStores.value.multiplication)

        // 2 - 2
        store1.dispatch(Inc)
        assertEquals(4, combinedStores.value.multiplication)

        // 2 - 3
        store1.dispatch(Inc)
        assertEquals(6, combinedStores.value.multiplication)

        // 3 - 3
        store0.dispatch(Inc)
        assertEquals(9, combinedStores.value.multiplication)
    }
}