package io.github.codebandits.beak

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AllOrErrorTest : TestWithDatabase() {

    @Test
    fun `should return a record for each item in the database`() {
        transaction {
            FeatherTable.insert {}
        }

        transaction {
            assertEquals(1, FeatherEntity.allOrError().assertRight().count())
        }
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        server.stop()

        transaction {
            val actualError: DataAccessError = FeatherEntity.allOrError().assertLeft()

            assertEquals(DataAccessError.SystemError.ConnectionError::class, actualError::class)
        }
    }

    @Test
    fun `should return a failure when there is no transaction`() {
        val actualError: DataAccessError = FeatherEntity.allOrError().assertLeft()

        assertEquals(DataAccessError.SystemError.TransactionError::class, actualError::class)
    }
}
