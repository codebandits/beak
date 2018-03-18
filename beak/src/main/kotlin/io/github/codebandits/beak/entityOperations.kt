package io.github.codebandits.beak

import arrow.core.Either
import arrow.core.flatMap
import arrow.data.Try
import io.github.codebandits.beak.DataAccessError.QueryError.NotFoundError
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.transactions.TransactionManager

/**
 * Get all the entities.
 *
 * @return A list of all the entities or a DataAccessError.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.allOrError(): Either<DataAccessError, List<T>> =
    Try { all().toList() }.mapFailureToDataAccessError()

/**
 * Create a new entity with the fields that are set in the [init] block. The id will be automatically set.
 *
 * @param init The block where the entity's fields can be set.
 *
 * @return The entity that has been created or a DataAccessError.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.newOrError(init: T.() -> Unit): Either<DataAccessError, T> =
    Try { new(init).also { TransactionManager.current().commit() } }.mapFailureToDataAccessError()

/**
 * Get an entity by its [id].
 *
 * @param id The id of the entity.
 *
 * @return The entity that has this id or a DataAccessError.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.findByIdOrError(id: ID): Either<DataAccessError, T> =
    Try { findById(id) }
        .mapFailureToDataAccessError()
        .flatMap {
            when (it) {
                null -> Either.left(NotFoundError(Throwable("Not found: the value returned from database was null")))
                else -> Either.right(it)
            }
        }

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.findOrError(op: SqlExpressionBuilder.() -> Op<Boolean>): Either<DataAccessError, List<T>> =
    Try { find(op).toList() }.mapFailureToDataAccessError()
