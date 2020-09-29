package dev.valvassori

import dev.valvassori.ext.value
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
fun interface Next<S> {
    operator fun invoke(
        action: Fluks.Action,
    ): S
}

@ExperimentalCoroutinesApi
fun interface Middleware<S : Fluks.State> {
    fun execute(
        store: Fluks.Store<S>,
        action: Fluks.Action,
        next: Next<S>,
    ): S
}

@ExperimentalCoroutinesApi
internal fun <S : Fluks.State> Fluks.Store<S>.createChain(
    middlewares: List<Middleware<S>>,
): ChainNode<S> {
    var finalNode = asChainNode()

    middlewares.asReversed().forEach { middleware ->
        finalNode = ChainNode(
            middleware = middleware,
            nextNode = finalNode,
        )
    }

    return finalNode
}

@ExperimentalCoroutinesApi
internal class ChainNode<S : Fluks.State>(
    private val middleware: Middleware<S>,
    private val nextNode: ChainNode<S>?,
) {

    private fun requireNext(store: Fluks.Store<S>): Next<S> = Next { action ->
        nextNode?.execute(store, action) ?: throw IllegalStateException("Next must not be null")
    }

    fun execute(store: Fluks.Store<S>, action: Fluks.Action): S = middleware.execute(
        store = store,
        action = action,
        next = requireNext(store),
    )
}

@ExperimentalCoroutinesApi
internal fun <S : Fluks.State> Fluks.Store<S>.asChainNode() = ChainNode<S>(
    middleware = { _, action, _ -> reduce(value, action) },
    nextNode = null
)