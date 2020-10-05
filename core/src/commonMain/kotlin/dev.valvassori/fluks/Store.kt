package dev.valvassori.fluks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
fun <S : Fluks.State> store(
    initialValue: S,
    context: CoroutineContext = Dispatchers.Default,
    reducer: Reducer<S>,
): Fluks.Store<S> = object : AbstractStore<S>(context) {
    override val initialValue: S
        get() = initialValue

    override fun reduce(
        currentState: S,
        action: Fluks.Action
    ): S = reducer.reduce(currentState, action)
}

@ExperimentalCoroutinesApi
abstract class AbstractStore<S : Fluks.State> constructor(
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

@ExperimentalCoroutinesApi
fun <S0 : Fluks.State, S1 : Fluks.State, SOUT : Fluks.State> combineStores(
    initialValue: SOUT,
    store0: Fluks.Store<S0>,
    store1: Fluks.Store<S1>,
    baseContext: CoroutineContext = Dispatchers.Default,
    combiner: Combiner<S0, S1, SOUT>
) = object : AbstractCombinedStore<S0, S1, SOUT>(store0, store1, baseContext) {
    override val initialValue: SOUT
        get() = initialValue

    override fun combine(state0: S0, state1: S1): SOUT = combiner.combine(state0, state1)
}

@ExperimentalCoroutinesApi
abstract class AbstractCombinedStore<S0 : Fluks.State, S1 : Fluks.State, SOUT : Fluks.State> constructor(
    store0: Fluks.Store<S0>,
    store1: Fluks.Store<S1>,
    baseContext: CoroutineContext = Dispatchers.Default,
) : Fluks.Store<SOUT>(baseContext), Combiner<S0, S1, SOUT> {

    init {
        store0.state
            .combine(store1.state) { s0, s1 -> combine(s0, s1) }
            .onEach { sout -> state.value = sout }
            .launchIn(scope)
    }

    @Deprecated("Combined stores just react to changes in the other stores")
    final override fun reduce(currentState: SOUT, action: Fluks.Action): SOUT = currentState

    @Deprecated("Combined stores just react to changes in the other stores")
    final override fun dispatch(action: Fluks.Action) {
    }

    @Deprecated("Combined stores just react to changes in the other stores")
    final override fun applyMiddleware(middleware: Middleware<SOUT>) {
    }

    @Deprecated("Combined stores just react to changes in the other stores")
    override fun applyMiddleware(middlewares: List<Middleware<SOUT>>) {
    }
}
