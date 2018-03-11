package io.github.codebandits.beak

import arrow.core.Either
import arrow.data.Try
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.allOrError(): Either<DataAccessError.SystemError, List<T>> =
    Try { all().toList() }.handleDataAccessErrors()

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.newOrError(init: T.() -> Unit): Either<DataAccessError, T> =
    Try { new(init).also { TransactionManager.current().commit() } }.handleDataAccessErrors()

private fun <T> Try<T>.handleDataAccessErrors(): Either<DataAccessError.SystemError, T> = toEither().mapLeft {
    when {
        it.cause is java.net.ConnectException -> DataAccessError.SystemError.ConnectionError(it)
        it.message == "No transaction in context." -> DataAccessError.SystemError.TransactionError(it)
        else -> throw UnexpectedException(it)
    }
}
