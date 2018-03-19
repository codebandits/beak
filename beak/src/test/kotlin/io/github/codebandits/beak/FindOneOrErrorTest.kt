package io.github.codebandits.beak

import io.github.codebandits.beak.DataAccessError.QueryError.MultipleFoundError
import io.github.codebandits.beak.DataAccessError.QueryError.NotFoundError
import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import io.github.codebandits.beak.DataAccessError.SystemError.TransactionError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class FindOneOrErrorH2Test : FindOneOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class FindOneOrErrorMysqlTest : FindOneOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

abstract class FindOneOrErrorTest : TestWithDatabase() {

    @Test
    fun `should return the matching record in the database`() {
        transaction {
            FeatherEntity.newOrError { type = "contour" }.assertRight()
            FeatherEntity.newOrError { type = "contour" }.assertRight()
            FeatherEntity.newOrError { type = "down" }.assertRight()
        }

        transaction {
            FeatherEntity.findOneOrError { FeatherTable.type eq "down" }.assertRight()
        }
    }

    @Test
    fun `should return a failure when no records match`() {
        transaction {
            FeatherEntity.newOrError { type = "contour" }.assertRight()
            FeatherEntity.newOrError { type = "contour" }.assertRight()
            FeatherEntity.newOrError { type = "down" }.assertRight()
        }

        transaction {
            val actualError: DataAccessError = FeatherEntity.findOneOrError { FeatherTable.type eq "filoplume" }.assertLeft()

            assertEquals(NotFoundError::class, actualError::class)
        }
    }

    @Test
    fun `should return a failure when multiple records match`() {
        transaction {
            FeatherEntity.newOrError { type = "contour" }.assertRight()
            FeatherEntity.newOrError { type = "contour" }.assertRight()
            FeatherEntity.newOrError { type = "down" }.assertRight()
        }

        transaction {
            val actualError: DataAccessError = FeatherEntity.findOneOrError { FeatherTable.type eq "contour" }.assertLeft()

            assertEquals(MultipleFoundError::class, actualError::class)
        }
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        transaction {
            val actualError: DataAccessError = FeatherEntity.findOneOrError { FeatherTable.type eq "contour" }.assertLeft()

            assertEquals(ConnectionError::class, actualError::class)
        }
    }

    @Test
    fun `should return a failure when there is no transaction`() {
        val actualError: DataAccessError = FeatherEntity.findOneOrError { FeatherTable.type eq "contour" }.assertLeft()

        assertEquals(TransactionError::class, actualError::class)
    }
}
