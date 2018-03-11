package io.github.codebandits.beak

import arrow.core.Either
import arrow.data.Try
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.allOrError(): Either<DataAccessError, List<T>> {
    return Try { all().toList() }.toEither()
        .mapLeft {
            when {
                it.cause is java.net.ConnectException      -> DataAccessError.SystemError.ConnectionError(it)
                it.message == "No transaction in context." -> DataAccessError.SystemError.TransactionError(it)
                else                                       -> unhandled(it)
            }
        }
}

private fun unhandled(throwable: Throwable): Nothing =
    throw Throwable("this exception is not handled by beak", throwable)
