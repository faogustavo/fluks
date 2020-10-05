# Fluks

<p align="center">
    <a href="https://bintray.com/faogustavo/maven/fluks"><img src="https://img.shields.io/badge/dynamic/json.svg?label=latest%20release&url=https%3A%2F%2Fapi.bintray.com%2F%2Fpackages%2Ffaogustavo%2Fmaven%2Ffluks%2Fversions%2F_latest&query=name&colorB=0094cd&style=for-the-badge" alt="Bintray"/></a>
    <a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/faogustavo/fluks.svg?style=for-the-badge" alt="License"/></a>
    <a href="https://github.com/faogustavo/fluks/issues"><img src="https://img.shields.io/github/issues/faogustavo/fluks.svg?style=for-the-badge" alt="GitHub issues"/></a>
</p>

<p align="center">
    <a href="/"><img src="https://img.shields.io/badge/Kotlin%20Version-1.4.0-blue?style=for-the-badge&logo=Kotlin" alt="GitHub top language"/></a>
    <a href="/"><img src="https://img.shields.io/github/languages/top/faogustavo/fluks.svg?style=for-the-badge&logoColor=white" alt="GitHub top language"/></a>
    <a href="/"><img src="https://img.shields.io/badge/KOTLIN%20MULTIPLATFORM-yes-green?style=for-the-badge" alt="GitHub top language"/></a>
</p>

Implementation of [flux application architecture](https://facebook.github.io/flux/) on top of [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) for multiplatform projects.

```kotlin
// Define your state
data class State(
    val count: Int
) : Fluks.State

// Define your actions
sealed class Action : Fluks.Action {
    object Inc : Action()
    object Dec : Action()
    class Mult(val multiplier: Int) : Action()
    class Div(val divider: Int) : Action()
}

// Create your store
private val store: Fluks.Store<State> = store(
    initialValue = State(0),
    reducer = reducer { state, action ->
        when(action) {
            is Action.Inc -> state.copy(
                 count = state.count + 1
            )
            is Action.Dec -> state.copy(
                count = state.count - 1
            )
            is Action.Mult -> state.copy(
                count = state.count * action.multiplier
            )
            is Action.Div -> state.copy(
                count = state.count / action.divider
            )
            else -> state
        }
    },
)

// Dispatch your actions
store.dispatch(Action.Inc)
store.dispatch(Action.Dec)
store.dispatch(Action.Mult(2))
store.dispatch(Action.Div(2))

// Use the state
val currentState = store.value
store.valueFlow
    .onEach { state -> /* do something */ }
    .launchIn(scope) 
```

## Installation

Add this implementation to you gradle file: 

```groovy
implementation "dev.valvassori.fluks:core:$fluks_version"
```

## Usage

### 1. Create your state class inheriting from `Fluks.State` ;

```kotlin
data class State(
    val count: Int
) : Fluks.State
```

### 2. Create your actions. They have to inherit from `Fluks.Action`;

```kotlin
sealed class Action : Fluks.Action {
    object Inc : Action()
    object Dec : Action()
    class Mult(val multiplier: Int) : Action()
    class Div(val divider: Int) : Action()
}
```

### 3. Create your store inheriting from `Fluks.Store` and implement the abstract methods;
    
In this step, you can opt for two variants. 

Inherit from `AbstractStore`;

```kotlin
private class Store : AbstractStore<State>() {
    override val initialValue: State
        get() = State(count = 0)

    override fun reduce(currentState: State, action: Fluks.Action): State =
        when (action) {
            is Action.Inc -> currentState.copy(
                count = currentState.count + 1
            )
            is Action.Dec -> currentState.copy(
                count = currentState.count - 1
            )
            is Action.Mult -> currentState.copy(
                count = currentState.count * action.multiplier
            )
            is Action.Div -> currentState.copy(
                count = currentState.count / action.divider
            )
            else -> currentState
        }
}

val store = Store()
```

or use the `store` helper function

```kotlin
val store: Fluks.Store<State> = store(
    initialValue = State(0),
    reducer = reducer { state, action ->
        when (action) {
            is Action.Inc -> state.copy(
                count = state.count + 1
            )
            is Action.Dec -> state.copy(
                count = state.count - 1
            )
            is Action.Mult -> state.copy(
                count = state.count * action.multiplier
            )
            is Action.Div -> state.copy(
                count = state.count / action.divider
            )
            else -> state
        }
    },
)
```

As you can see, in both of them, you must provide an initialValue and a reducer.

### 4. (Optional) Create your reducer

When you are using the `store` helper function, you can create reducer apart from
the function call to improve readability and make it easier to test.

```kotlin
// You can also use the `Reducer` fun interface with the capital 'R'.
val storeReducer = reducer { currentState, action -> 
    when(action) {
        is Action.Inc -> state.copy(
             count = currentState.count + 1
        )
        is Action.Dec -> currentState.copy(
            count = currentState.count - 1
        )
        is Action.Mult -> currentState.copy(
            count = currentState.count * action.multiplier
        )
        is Action.Div -> currentState.copy(
            count = currentState.count / action.divider
        )
        else -> state
    }
}
```

### 5. Dispatch your actions to the store using the `.dispatch(Fluks.Action)` method from the store;

After having your store instance, you can dispatch your actions.

```kotlin
store.dispatch(Action.Inc)
store.dispatch(Action.Dec)
store.dispatch(Action.Mult(2))
store.dispatch(Action.Div(2))
```

### 6. (Optional) Adding middlewares

When required, you can add middlewares to help you update some dispatched action.
The middlewares are executed in a chain, and the last node is the reducer.

To add a new middleware, create a new one using the `Middleware` fun interface.
Then, implement the lambda with the three required parameters and return the updated state:

* Store: The store that dispatched the action 
* Action: The action that has been dispatched
* Next: The next node from the chain

```kotlin
val stateLogMiddleware = Middleware<State> { store, action, next ->
    val messages = mutableListOf(
        "[Old State]: ${store.value.count}",
        "[Action]: ${action::class.simpleName}",
    )

    val updatedState = next(action)

    messages.add("[New State]: ${updatedState.count}")
    messages.forEach { logger.log(it) }

    updatedState
}
```

After having an instance of your middleware, apply it to the store that you need.

```kotlin
// For one middleware only
store.applyMiddleware(stateLogMiddleware)

// For multiple middlewares
store.applyMiddleware(listOf(stateLogMiddleware))
```

Be careful with the `applyMiddleware` function if you have multiple middlewares. Each time you call this function,
a new chain is created and overwrites the previous one.

## Global Dispatcher

In some scenarios, you will need to dispatch an action to all of your stores (like a logout to clear
the user content). If this is the case, we have a global function called `dispatch(Fluks.Action)` that 
receives and action and calls all your stores.

```kotlin
object Logout : Fluks.Action

val accountStore = store(emptyAccountState, accountReducer)
val ordersStore = store(emptyOrdersState, ordersReducer)

dispatch(Logout)

assertFalse(accountStore.value.isUserLoggedIn)
assertTrue(ordersStore.value.orders.isEmpty())
```

## Next steps

- [ ] Improve unit tests
- [ ] Improve documentation
- [ ] Github CI to run checks
- [ ] Integration for android Jetpack Compose