package io.github.codebandits.beak

import arrow.core.Either
import arrow.core.flatMap
import arrow.data.Try
import io.github.codebandits.beak.DataAccessError.QueryError.NotFoundError
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Get all the entities.
 *
 * @return A list of all the entities or a DataAccessError.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.allOrError(): Either<DataAccessError, List<T>> =
    Try { transaction { all().toList() } }.mapFailureToDataAccessError()

/**
 * Create a new entity with the fields that are set in the [init] block. The id will be automatically set.
 *
 * @param init The block where the entity's fields can be set.
 *
 * @return The entity that has been created or a DataAccessError.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.newOrError(init: T.() -> Unit): Either<DataAccessError, T> =
    Try { transaction { new(init) } }.mapFailureToDataAccessError()

/**
 * Count the amount of entities that conform to the [op] statement.
 *
 * @param op The statement to count the entities for. The statement must be of boolean type.
 *
 * @return The amount of entities that conform to the [op] statement.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.countOrError(op: Op<Boolean>? = null): Either<DataAccessError, Int> {
    return Try { transaction { count(op) } }.mapFailureToDataAccessError()
}

/**
 * Get an entity by its [id].
 *
 * @param id The id of the entity.
 *
 * @return The entity that has this id or a DataAccessError.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.findByIdOrError(id: ID): Either<DataAccessError, T> =
    Try { transaction { findById(id) } }
        .mapFailureToDataAccessError()
        .flatMap {
            when (it) {
                null -> Either.left(NotFoundError(NoSuchElementException("Not found: the value returned from database was null")))
                else -> Either.right(it)
            }
        }

/**
 * Get all the entities that conform to the [op] statement.
 *
 * @param op The statement with which to select the entities. The statement must be of boolean type.
 *
 * @return All the entities that conform to the [op] statement or a DataAccessError.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.findWhereOrError(op: SqlExpressionBuilder.() -> Op<Boolean>): Either<DataAccessError, List<T>> =
    Try { transaction { find(op).toList() } }.mapFailureToDataAccessError()

/**
 * Get the entity that conforms to the [op] statement.
 *
 * @param op The statement with which to select the entity. The statement must be of boolean type.
 *
 * @return The one entity that conforms to the [op] statement or a DataAccessError.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.findOneOrError(op: SqlExpressionBuilder.() -> Op<Boolean>): Either<DataAccessError, T> =
    Try { transaction { find(op).single() } }.mapFailureToDataAccessError()

/**
 * Delete an entity by its [id].
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.deleteOrError(id: ID): Either<DataAccessError, Unit> =
    findByIdOrError(id).flatMap { Try { transaction { it.delete() } }.mapFailureToDataAccessError() }

/**
 * Update an entity by its [id].
 *
 * @param id The id of the entity.
 * @param update The block where the entity's fields can be set.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.updateByIdOrError(
    id: ID,
    update: T.() -> Unit
): Either<DataAccessError, Unit> =
    findByIdOrError(id)
        .flatMap { Try { it.apply(update) }.mapFailureToDataAccessError() }
        .map { Unit }

/**
 * Update the entities that match a selection.
 *
 * @param op The statement with which to select the entities. The statement must be of boolean type.
 * @param update The block where the entity's fields can be set.
 */
fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.updateWhereOrError(
    op: SqlExpressionBuilder.() -> Op<Boolean>,
    update: T.() -> Unit
): Either<DataAccessError, Unit> =
    findWhereOrError(op)
        .flatMap {
            if (it.isNotEmpty()) Either.right(it)
            else Either.left(NotFoundError(NoSuchElementException("Not found: no matching entities found in the database")))
        }
        .flatMap { Try { it.map { it.apply(update) } }.mapFailureToDataAccessError() }
        .map { Unit }
