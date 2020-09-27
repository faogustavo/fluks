package dev.valvassori

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalCoroutinesApi
fun <S : Fluks.State> store(
    initialValue: S,
    reducer: Reducer<S>
): Fluks.Store<S> = object : Fluks.Store<S>() {
    override val initialValue: S
        get() = initialValue

    override fun reduce(
        currentState: S,
        action: Fluks.Action
    ): S = reducer.reduce(currentState, action)
}

@ExperimentalCoroutinesApi
object Fluks {
    interface Action

    interface State

    abstract class Store<S : State> : Dispatcher {

        abstract val initialValue: S
        abstract fun reduce(currentState: S, action: Action): S

        private val _state by lazy { MutableStateFlow(initialValue) }
        private val _queue: Channel<Action> by lazy { Channel(Channel.UNLIMITED) }
        internal val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        val value: S
            get() = _state.value

        val valueFlow: Flow<S>
            get() = _state

        init {
            scope.launch {
                for (action in _queue) {
                    val currentState = _state.value

                    val newState = reduce(
                        currentState = currentState,
                        action = action
                    )

                    _state.value = newState
                }
            }
        }

        override fun dispatch(action: Action) {
            _queue.offer(action)
        }
    }
}