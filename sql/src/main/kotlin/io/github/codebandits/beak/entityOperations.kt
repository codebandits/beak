package io.github.codebandits.beak

import arrow.core.Either
import arrow.core.flatMap
import arrow.data.Try
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.allOrError(): Either<DataAccessError, List<T>> =
    Try { all().toList() }.mapFailureToDataAccessError()

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.newOrError(init: T.() -> Unit): Either<DataAccessError, T> =
    Try { new(init).also { TransactionManager.current().commit() } }.mapFailureToDataAccessError()

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.findByIdOrError(id: ID): Either<DataAccessError, T> =
    Try { findById(id) }
        .mapFailureToDataAccessError()
        .flatMap {
            when (it) {
                null -> Either.left(DataAccessError.EntityError.NotFoundError(Throwable("Not found")))
                else -> Either.right(it)
            }
        }

private fun <T> Try<T>.mapFailureToDataAccessError(): Either<DataAccessError, T> = toEither().mapLeft {
    when {
        it.cause is java.net.ConnectException      -> DataAccessError.SystemError.ConnectionError(it)
        it.message == "No transaction in context." -> DataAccessError.SystemError.TransactionError(it)
        else                                       -> throw UnexpectedException(it)
    }
}
