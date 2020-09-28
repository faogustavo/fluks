package dev.valvassori

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
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

        internal val reducer: Reducer<S> = Reducer { currentState, action -> reduce(currentState, action) }
        internal val state by lazy { MutableStateFlow(initialValue) }
        internal val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        private val _queue: Channel<Action> by lazy { Channel(Channel.UNLIMITED) }
        private var _middlewares: ChainNode<S> = asChainNode()

        init {
            scope.launch {
                for (action in _queue) {
                    state.value = _middlewares.execute(
                        store = this@Store,
                        action = action
                    )
                }
            }
        }

        override fun dispatch(action: Action) {
            _queue.offer(action)
        }

        fun applyMiddleware(middleware: Middleware<S>) {
            applyMiddlewares(listOf(middleware))
        }

        fun applyMiddlewares(middlewares: List<Middleware<S>>) {
            _middlewares = createChain(middlewares)
        }
    }
}