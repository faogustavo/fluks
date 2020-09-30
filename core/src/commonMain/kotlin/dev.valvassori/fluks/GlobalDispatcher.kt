package dev.valvassori.fluks

import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
fun dispatch(action: Fluks.Action) {
    GlobalDispatcher.dispatch(action)
}

@ExperimentalCoroutinesApi
internal object GlobalDispatcher {

    private val stores: MutableList<Fluks.Store<*>> = mutableListOf()

    internal fun dispatch(action: Fluks.Action) {
        stores.forEach { it.dispatch(action) }
    }

    internal fun register(store: Fluks.Store<*>) {
        stores.add(store)
    }
}