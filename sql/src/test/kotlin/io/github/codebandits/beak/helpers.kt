package io.github.codebandits.beak

import arrow.core.Either
import kotlin.test.fail

fun <A, B> Either<A, B>.assertRight(): B {
    return when (this) {
        is Either.Left  -> fail("expected Right but was Left: $this")
        is Either.Right -> b
    }
}

fun <A, B> Either<A, B>.assertLeft(): A {
    return when (this) {
        is Either.Left  -> a
        is Either.Right -> fail("expected Left but was Right: $this")
    }
}
