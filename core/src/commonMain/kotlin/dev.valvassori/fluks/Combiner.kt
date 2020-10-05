package dev.valvassori.fluks

import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
fun <S0 : Fluks.State, S1 : Fluks.State, SOUT : Fluks.State> combiner(
    block: (S0, S1) -> SOUT,
): Combiner<S0, S1, SOUT> = Combiner { s0, s1 -> block(s0, s1) }

@ExperimentalCoroutinesApi
fun interface Combiner<S0 : Fluks.State, S1 : Fluks.State, SOUT : Fluks.State> {
    fun combine(state0: S0, state1: S1): SOUT
}