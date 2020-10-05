package dev.valvassori.fluks

import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
fun interface Dispatcher {
    fun dispatch(action: Fluks.Action)
}
