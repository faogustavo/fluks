package dev.valvassori.fluks.middlewares

import dev.valvassori.fluks.Fluks
import dev.valvassori.fluks.Middleware
import dev.valvassori.fluks.ext.value
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun interface Logger {
    fun log(value: String)
}

@ExperimentalCoroutinesApi
fun <S : Fluks.State> logMiddleware(
    logger: Logger = Logger { println(it) }
): Middleware<S> = Middleware { store, action, next ->
    val messages = mutableListOf(
        "[Old State]: ${store.value}",
        "[Action]: ${action::class.simpleName}",
    )

    val updatedValue = next(action)

    messages.add("[New State]: $updatedValue")
    messages.forEach { logger.log(it) }

    updatedValue
}