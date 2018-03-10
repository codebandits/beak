package io.github.codebandits.beak

import arrow.core.Either
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.SizedIterable

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.allOrError(): Either<DataAccessError, SizedIterable<T>> =
    Either.right(all()).mapLeft<DataAccessError> { TODO() }
