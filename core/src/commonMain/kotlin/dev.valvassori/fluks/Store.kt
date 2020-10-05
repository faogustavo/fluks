package dev.valvassori.fluks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
fun <S : Fluks.State> store(
    initialValue: S,
    reducer: Reducer<S>,
    context: CoroutineContext = Dispatchers.Default,
): Fluks.Store<S> = object : AbstractStore<S>(context) {
    override val initialValue: S
        get() = initialValue

    override fun reduce(
        currentState: S,
        action: Fluks.Action
    ): S = reducer.reduce(currentState, action)
}

@ExperimentalCoroutinesApi
abstract class AbstractStore <S : Fluks.State> constructor(
    baseContext: CoroutineContext = Dispatchers.Default,
) : Fluks.Store<S>(baseContext) {

    private val _queue: Channel<Fluks.Action> by lazy { Channel(Channel.UNLIMITED) }
    private var _middlewares: ChainNode<S> = asChainNode()

    init {
        scope.launch {
            register()
            for (action in _queue) {
                state.value = _middlewares.execute(
                    store = this@AbstractStore,
                    action = action
                )
            }
        }
    }

    final override fun dispatch(action: Fluks.Action) {
        _queue.offer(action)
    }

    final override fun applyMiddleware(middleware: Middleware<S>) {
        applyMiddleware(listOf(middleware))
    }

    final override fun applyMiddleware(middlewares: List<Middleware<S>>) {
        _middlewares = createChain(middlewares)
    }

    private fun register() {
        GlobalDispatcher.register(this)
    }
}