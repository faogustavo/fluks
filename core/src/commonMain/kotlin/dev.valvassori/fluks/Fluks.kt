package dev.valvassori.fluks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
object Fluks {
    interface Action

    interface State

    abstract class Store<S : State> constructor(
        baseContext: CoroutineContext = Dispatchers.Default,
    ) : Dispatcher {

        abstract val initialValue: S
        abstract fun reduce(currentState: S, action: Action): S

        internal val state by lazy { MutableStateFlow(initialValue) }
        internal val scope = CoroutineScope(baseContext + SupervisorJob())

        open fun addMiddleware(middleware: Middleware<S>) {}
        open fun applyMiddleware(middleware: Middleware<S>) {}
        open fun applyMiddleware(middlewares: List<Middleware<S>>) {}
    }
}
