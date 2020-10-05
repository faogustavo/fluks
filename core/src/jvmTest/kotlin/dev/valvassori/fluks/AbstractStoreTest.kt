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
internal class AbstractStoreTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    data class State(val count: Int) : Fluks.State

    sealed class Action : Fluks.Action {
        object Inc : Action()
        object Dec : Action()
        class Mult(val multiplier: Int) : Action()
        class Div(val divider: Int) : Action()
    }

    private val store: Fluks.Store<State> by lazy {
        store(
            initialValue = State(0),
            context = Dispatchers.Main
        ) { state, action ->
            when (action) {
                is Action.Inc -> state.copy(
                    count = state.count + 1
                )
                is Action.Dec -> state.copy(
                    count = state.count - 1
                )
                is Action.Mult -> state.copy(
                    count = state.count * action.multiplier
                )
                is Action.Div -> state.copy(
                    count = state.count / action.divider
                )
                else -> state
            }
        }
    }

    @Test
    fun dispatch_shouldProcessActionsCorrectly() = coroutineTestRule.runBlockingTest {
        store.dispatch(Action.Inc)
        store.dispatch(Action.Inc)
        store.dispatch(Action.Inc)
        store.dispatch(Action.Mult(3))
        store.dispatch(Action.Dec)
        store.dispatch(Action.Div(2))


        assertEquals(4, store.value.count)
    }

    @Test
    fun dispatch_withMiddleware_shouldProcessActionsCorrectly() = coroutineTestRule.runBlockingTest {
        val invertMiddleware = Middleware<State> { _, action, next ->
            val invertAction = when (action) {
                is Action.Inc -> Action.Dec
                is Action.Dec -> Action.Inc
                is Action.Mult -> Action.Div(action.multiplier)
                is Action.Div -> Action.Mult(action.divider)
                else -> action
            }

            next(invertAction)
        }

        store.applyMiddleware(invertMiddleware)

        store.dispatch(Action.Inc)
        store.dispatch(Action.Inc)
        store.dispatch(Action.Inc)
        store.dispatch(Action.Div(3))
        store.dispatch(Action.Dec)
        store.dispatch(Action.Mult(2))


        assertEquals(-4, store.value.count)
    }
}