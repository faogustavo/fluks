@file:JvmName("StoreExtKt")
package dev.valvassori.fluks.ext

import dev.valvassori.fluks.Fluks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmName

@ExperimentalCoroutinesApi
val <S : Fluks.State> Fluks.Store<S>.value: S
    get() = state.value

@ExperimentalCoroutinesApi
val <S : Fluks.State> Fluks.Store<S>.valueFlow: Flow<S>
    get() = state