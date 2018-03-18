package io.github.codebandits.beak

import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class NewOrErrorH2Test : NewOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class NewOrErrorMysqlTest : NewOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

abstract class NewOrErrorTest : TestWithDatabase() {

    @Test
    fun `should create a record in the database`() {
        transaction {
            FeatherEntity.newOrError {}.assertRight()
        }

        transaction {
            assertEquals(1, FeatherEntity.allOrError().assertRight().count())
        }
    }

    @Test
    fun `should save the data into a record in the database`() {
        transaction {
            val feather = FeatherEntity.newOrError {
                type = "contour"
            }.assertRight()

            assertEquals("contour", feather.type)
        }
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        transaction {
            val actualError: DataAccessError = FeatherEntity.newOrError {}.assertLeft()

            assertEquals(DataAccessError.SystemError.ConnectionError::class, actualError::class)
        }
    }

    @Test
    fun `should return a failure when there is no transaction`() {
        val actualError: DataAccessError = FeatherEntity.newOrError {}.assertLeft()

        assertEquals(DataAccessError.SystemError.TransactionError::class, actualError::class)
    }

    @Test
    fun `should return a failure when the data is invalid`() {
        transaction {
            val actualError: DataAccessError = FeatherEntity.newOrError {
                type = "x".repeat(500)
            }.assertLeft()

            assertEquals(DataAccessError.QueryError.BadRequestError::class, actualError::class)
        }
    }
}
