package io.github.codebandits.beak

/**
 * This exception is thrown by Beak when it does not know how to translate a given Throwable into a DataAccessError.
 *
 * @param cause the cause of this exception.
 */
class UnexpectedException(cause: Throwable) : Exception("this exception is not handled by Beak", cause)
