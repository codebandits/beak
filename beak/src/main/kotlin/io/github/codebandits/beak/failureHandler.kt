package io.github.codebandits.beak

import arrow.core.Either
import arrow.core.Try
import arrow.core.flatMap
import io.github.codebandits.beak.DataAccessError.QueryError.*
import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import io.github.codebandits.beak.DataAccessError.SystemError.TransactionError

internal fun <T> Either<DataAccessError, T?>.notNull(): Either<DataAccessError, T> =
    flatMap {
        when (it) {
            null -> Either.left(NotFoundError(NoSuchElementException("Not found: the value returned from database was null")))
            else -> Either.right(it)
        }
    }

internal fun <T> Try<T>.mapFailureToDataAccessError(): Either<DataAccessError, T> =
    toEither()
        .mapLeft { throwable ->
            Either.left(throwable)
                .handleGenericThrowables()
                .handleMysqlThrowables()
                .handleH2Throwables()
                .handlePostgresqlThrowables()
                .fold({ throw UnexpectedException(it) }, { it })
        }

private fun Either<Throwable, DataAccessError>.handleMysqlThrowables() = maybeHandleThrowable { throwable ->
    when {
        classIsPresent("com.mysql.cj.jdbc.Driver") -> when (throwable.cause) {
            is com.mysql.cj.jdbc.exceptions.CommunicationsException -> Either.right(ConnectionError(throwable))
            is com.mysql.cj.jdbc.exceptions.MysqlDataTruncation     -> Either.right(BadRequestError(throwable))
            else                                                    -> this
        }
        else                                       -> this
    }
}

private fun Either<Throwable, DataAccessError>.handleH2Throwables() = maybeHandleThrowable { throwable ->
    when {
        classIsPresent("org.h2.Driver") -> when (throwable.cause) {
            is org.h2.jdbc.JdbcSQLException -> Either.right(BadRequestError(throwable))
            else                            -> this
        }
        else                            -> this
    }
}

private fun Either<Throwable, DataAccessError>.handlePostgresqlThrowables() = maybeHandleThrowable { throwable ->
    when {
        classIsPresent("org.postgresql.Driver") -> when (throwable.cause) {
            is org.postgresql.util.PSQLException -> Either.right(BadRequestError(throwable))
            else                                 -> this
        }
        else                                    -> this
    }
}

private fun Either<Throwable, DataAccessError>.handleGenericThrowables() = maybeHandleThrowable { throwable ->
    when (throwable) {
        is java.util.NoSuchElementException                     -> Either.right(NotFoundError(throwable))
        is java.lang.IllegalArgumentException                   -> when {
            throwable.message == "Collection has more than one element." -> Either.right(MultipleFoundError(throwable))
            else                                                         -> this
        }
        is java.lang.IllegalStateException                      -> when {
            throwable.message == "No transaction in context." -> Either.right(TransactionError(throwable))
            else                                              -> this
        }

        is org.jetbrains.exposed.exceptions.ExposedSQLException -> {
            when {
                throwable.cause is java.sql.BatchUpdateException    -> Either.right(BadRequestError(throwable))
                throwable.cause?.cause is java.net.ConnectException -> Either.right(ConnectionError(throwable))
                else                                                -> this
            }
        }
        else                                                    -> this
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
