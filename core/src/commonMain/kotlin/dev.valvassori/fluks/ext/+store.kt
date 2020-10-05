@file:JvmName("StoreExtKt")
package dev.valvassori.fluks.ext

import dev.valvassori.fluks.Combiner
import dev.valvassori.fluks.Fluks
import dev.valvassori.fluks.combineStores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmName

@ExperimentalCoroutinesApi
val <S : Fluks.State> Fluks.Store<S>.value: S
    get() = state.value

@ExperimentalCoroutinesApi
val <S : Fluks.State> Fluks.Store<S>.valueFlow: Flow<S>
    get() = state

@ExperimentalCoroutinesApi
fun <S0 : Fluks.State, S1 : Fluks.State, SOUT : Fluks.State> Fluks.Store<S0>.combineWith(
    initialValue: SOUT,
    other: Fluks.Store<S1>,
    baseContext: CoroutineContext = Dispatchers.Default,
    combiner: Combiner<S0, S1, SOUT>,
) = combineStores(
    initialValue = initialValue,
    store0 = this,
    store1 = other,
    baseContext = baseContext,
    combiner = combiner
)