package dev.valvassori

import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
fun <S : Fluks.State> reducer(
    reduceLambda: (S, Fluks.Action) -> S
): Reducer<S> = Reducer { currentState, action -> reduceLambda(currentState, action) }

@ExperimentalCoroutinesApi
fun interface Reducer<S : Fluks.State> {
    fun reduce(
        currentState: S,
        action: Fluks.Action
    ): S
}