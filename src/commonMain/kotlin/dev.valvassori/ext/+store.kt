package dev.valvassori.ext

import dev.valvassori.Fluks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@ExperimentalCoroutinesApi
val <S : Fluks.State> Fluks.Store<S>.value: S
    get() = state.value

@ExperimentalCoroutinesApi
val <S : Fluks.State> Fluks.Store<S>.valueFlow: Flow<S>
    get() = state