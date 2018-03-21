package io.github.codebandits.beak

import io.github.codebandits.beak.DataAccessError.QueryError.NotFoundError
import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class UpdateByIdOrErrorH2Test : UpdateByIdOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class UpdateByIdOrErrorMysqlTest : UpdateByIdOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class UpdateByIdOrErrorPostgresqlTest : UpdateByIdOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class UpdateByIdOrErrorTest : TestWithDatabase() {

    @Test
    fun `should update an entity from the table`() {
        val id = transaction {
            val expectedEntity = FeatherEntity.newOrError {
                type = "contour"
            }.assertRight()

            val id = expectedEntity.id.value

            FeatherEntity.updateByIdOrError(id) {
                type = "contour-updated"
            }.assertRight()

            id
        }

        transaction {
            assertEquals(
                "contour-updated",
                FeatherEntity.findByIdOrError(id).assertRight().type
            )
        }
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        transaction {
            assertEquals(
                ConnectionError::class,
                FeatherEntity.updateByIdOrError(0L) { type = "should not happen" }.assertLeft()::class
            )
        }
    }

    @Test
    fun `should return a failure when there is no entities to update`() {
        transaction {
            assertEquals(
                NotFoundError::class,
                FeatherEntity.updateByIdOrError(0L) { type = "error" }.assertLeft()::class
            )
        }
    }
}
