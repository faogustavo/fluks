package demo

import dev.valvassori.Fluks
import dev.valvassori.ext.valueFlow
import dev.valvassori.middlewares.logMiddleware
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class MainState(val count: Int) : Fluks.State

class Increment(val quantity: Int = 1) : Fluks.Action
class Decrement(val quantity: Int = 1) : Fluks.Action

class MainStore : Fluks.Store<MainState>() {
    override val initialValue: MainState
        get() = MainState(0)

    override fun reduce(
        currentState: MainState,
        action: Fluks.Action
    ): MainState = when (action) {
        is Increment -> onIncrement(currentState, action)
        is Decrement -> onDecrement(currentState, action)
        else -> currentState
    }

    private fun onIncrement(
        currentState: MainState,
        action: Increment
    ) = currentState.copy(count = currentState.count + action.quantity)

    private fun onDecrement(
        currentState: MainState,
        action: Decrement
    ) = currentState.copy(count = currentState.count - action.quantity)
}

fun main() = runBlocking {
    val store = MainStore().apply {
        applyMiddleware(logMiddleware())
    }

    val job = store
        .valueFlow
        .onEach { "Store with state: $it".printLog() }
        .launchIn(this)

    launch {
        store.dispatch(Increment())
        delay(50)

        store.dispatch(Increment())
        delay(50)

        store.dispatch(Decrement(10))
        delay(50)

        store.dispatch(Increment(18))
        delay(50)
    }.join()

    job.cancel()
}

private fun String.printLog() {
    println()
    println(this)
    println()
}