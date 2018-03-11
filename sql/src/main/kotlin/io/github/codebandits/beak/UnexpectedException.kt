package io.github.codebandits.beak

class UnexpectedException(cause: Throwable) : Exception("this exception is not handled by beak", cause)
