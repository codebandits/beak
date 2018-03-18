package io.github.codebandits.beak

import arrow.core.Either
import arrow.core.flatMap
import arrow.data.Try
import io.github.codebandits.beak.DataAccessError.QueryError.NotFoundError
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
                null -> Either.left(NotFoundError(Throwable("Not found: the value returned from database was null")))
                else -> Either.right(it)
            }
        }
