package io.github.codebandits.beak

import arrow.core.flatMap
import io.github.codebandits.beak.DataAccessError.QueryError.BadRequestError
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class TransactionH2RollbackTest : TransactionRollbackTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class TransactionMysqlRollbackTest : TransactionRollbackTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class TransactionPostgresqlRollbackTest : TransactionRollbackTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class TransactionRollbackTest : TestWithDatabase() {

    @Test
    fun `with an outer transaction, when an operation fails, prior operations in transaction should be rolled back`() {
        val error = beakTransaction {
            FeatherEntity.newOrError { }
                .flatMap { FeatherEntity.newOrError { type = "x".repeat(500) } }
        }.assertLeft()

        assertEquals(BadRequestError::class, error::class)
        assertEquals(0, FeatherEntity.countOrError().assertRight())
    }

    @Test
    fun `without an outer transaction, when an operation fails, prior operations in transaction should remain committed`() {
        val error = FeatherEntity.newOrError { }
            .flatMap { FeatherEntity.newOrError { type = "x".repeat(500) } }
            .assertLeft()

        assertEquals(BadRequestError::class, error::class)
        assertEquals(1, FeatherEntity.countOrError().assertRight())
    }
}
