package io.github.codebandits.beak

import arrow.core.Either
import arrow.data.Try
import io.github.codebandits.beak.DataAccessError.QueryError.BadRequestError
import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import io.github.codebandits.beak.DataAccessError.SystemError.TransactionError

internal fun <T> Try<T>.mapFailureToDataAccessError(): Either<DataAccessError, T> =
    toEither()
        .mapLeft { throwable ->
            Either.left(throwable)
                .handleGenericThrowables()
                .handleMysqlThrowables()
                .handleH2Throwables()
                .fold({ throw UnexpectedException(it) }, { it })
        }

private fun Either<Throwable, DataAccessError>.handleMysqlThrowables() = maybeHandleThrowable { throwable ->
    when {
        classIsPresent("com.mysql.cj.jdbc.Driver") -> when (throwable) {
            is com.mysql.cj.jdbc.exceptions.CommunicationsException -> Either.right(ConnectionError(throwable))
            else                                                    -> this
        }
        else                                       -> this
    }
}

private fun Either<Throwable, DataAccessError>.handleH2Throwables() = maybeHandleThrowable { throwable ->
    when {
        classIsPresent("org.h2.Driver") -> when {
            throwable.cause is java.net.ConnectException -> Either.right(ConnectionError(throwable))
            else                                         -> this
        }
        else                            -> this
    }
}

private fun Either<Throwable, DataAccessError>.handleGenericThrowables() = maybeHandleThrowable { throwable ->
    when {
        throwable is java.sql.BatchUpdateException        -> Either.right(BadRequestError(throwable))
        throwable.message == "No transaction in context." -> Either.right(TransactionError(throwable))
        else                                              -> this
    }
}

private fun Either<Throwable, DataAccessError>.maybeHandleThrowable(
    fn: (throwable: Throwable) -> Either<Throwable, DataAccessError>
): Either<Throwable, DataAccessError> {
    return when (this) {
        is Either.Left  -> fn(this.a)
        is Either.Right -> this
    }
}

private fun classIsPresent(className: String): Boolean {
    return try {
        Class.forName(className); true
    } catch (e: Throwable) {
        false
    }
}
