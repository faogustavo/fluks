package dev.valvassori.fluks

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
fun <S : Fluks.State> store(
    initialValue: S,
    reducer: Reducer<S>,
    context: CoroutineContext = Dispatchers.Default,
): Fluks.Store<S> = object : Fluks.Store<S>(context) {
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

    abstract class Store<S : State> constructor(
        baseContext: CoroutineContext = Dispatchers.Default,
    ) : Dispatcher {

        abstract val initialValue: S
        abstract fun reduce(currentState: S, action: Action): S

        internal val reducer: Reducer<S> = Reducer { currentState, action -> reduce(currentState, action) }
        internal val state by lazy { MutableStateFlow(initialValue) }
        internal val scope = CoroutineScope(baseContext + SupervisorJob())

        private val _queue: Channel<Action> by lazy { Channel(Channel.UNLIMITED) }
        private var _middlewares: ChainNode<S> = asChainNode()

        init {
            scope.launch {
                register()
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
            applyMiddleware(listOf(middleware))
        }

        fun applyMiddleware(middlewares: List<Middleware<S>>) {
            _middlewares = createChain(middlewares)
        }

        private fun register() {
            GlobalDispatcher.register(this)
        }
    }
}