package io.github.codebandits.beak

import arrow.core.Either
import arrow.core.Try
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Open a transaction to link rollbacks for all the data access operations within [statement].
 *
 * @param statement A lambda expression to be executed within a transaction.
 * The result of [statement] will be in returned when successful.
 *
 * @return The result of [statement] or a DataAccessError.
 */
fun <T> beakTransaction(statement: Transaction.() -> Either<DataAccessError, T>): Either<DataAccessError, T> =
    Try { transaction(statement = statement) }
        .mapFailureToDataAccessError()
        .fold({ Either.left(it) }, { it })
