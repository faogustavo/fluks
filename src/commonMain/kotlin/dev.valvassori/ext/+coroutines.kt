package dev.valvassori.ext

import dev.valvassori.Fluks
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi
fun Fluks.Store<*>.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend Fluks.Store<*>.() -> Unit
) = scope.launch(context, start) { block() }

@ExperimentalCoroutinesApi
fun Fluks.Store<*>.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend Fluks.Store<*>.() -> Unit
) = scope.async(context, start) { block() }
