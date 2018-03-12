package io.github.codebandits.beak

import arrow.core.Either
import arrow.data.Try
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.allOrError(): Either<DataAccessError, List<T>> =
        Try { all().toList() }.handleDataAccessErrors()

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.newOrError(init: T.() -> Unit): Either<DataAccessError, T> =
        Try { new(init).also { TransactionManager.current().commit() } }.handleDataAccessErrors()

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.findByIdOrError(id: ID): Either<DataAccessError, T> =
        Try { findById(id) }
                .catchEntityErrors()
                .handleDataAccessErrors()

private fun <T> Try<T?>.catchEntityErrors(): Try<T> {
    return flatMap {
        if (it == null) {
            Try.raise(Throwable("Not found"))
        } else {
            Try.pure(it)
        }
    }
}

private fun <T> Try<T>.handleDataAccessErrors(): Either<DataAccessError, T> = toEither().mapLeft {
    when {
        it.cause is java.net.ConnectException -> DataAccessError.SystemError.ConnectionError(it)
        it.message == "No transaction in context." -> DataAccessError.SystemError.TransactionError(it)
        it.message == "Not found" -> DataAccessError.EntityError.NotFoundError(it)
        else -> throw UnexpectedException(it)
    }
}
