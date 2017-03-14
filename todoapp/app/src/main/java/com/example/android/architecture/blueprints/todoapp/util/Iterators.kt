package com.example.android.architecture.blueprints.todoapp.util

/**
 * Performs the given [operation] on each element of this [Iterator].
 */
inline fun <T> MutableIterator<T>.removeIf(predicate: (T) -> Boolean) : Unit {
  while (hasNext()) {
    val entry = next()
    if (predicate(entry)) {
      remove()
    }
  }
}
