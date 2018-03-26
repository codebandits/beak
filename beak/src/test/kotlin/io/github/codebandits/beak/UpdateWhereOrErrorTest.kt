package io.github.codebandits.beak

import io.github.codebandits.beak.DataAccessError.QueryError.NotFoundError
import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import io.github.codebandits.beak.DataAccessError.SystemError.TransactionError
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class UpdateWhereOrErrorH2Test : UpdateWhereOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class UpdateWhereOrErrorMysqlTest : UpdateWhereOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class UpdateWhereOrErrorPostgresqlTest : UpdateWhereOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class UpdateWhereOrErrorTest : TestWithDatabase() {

    @Test
    fun `should update multiple entities that returns from query from the table`() {
        transaction {
            FeatherEntity.newOrError {
                type = "contour"
            }.assertRight()

            FeatherEntity.newOrError {
                type = "contour"
            }.assertRight()

            FeatherEntity.updateWhereOrError({ FeatherTable.type eq "contour" }) {
                type = "contour-updated"
            }.assertRight()
        }

        transaction {
            assertEquals(
                2,
                FeatherEntity.findWhereOrError({ FeatherTable.type eq "contour-updated" }).assertRight().count()
            )
        }
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        transaction {
            val actualError: DataAccessError = FeatherEntity.updateWhereOrError({ FeatherTable.id eq 0L }) {
                type = "should not happen"
            }.assertLeft()

            assertEquals(ConnectionError::class, actualError::class)
        }
    }

    @Test
    fun `should return a failure when there is no entities to update`() {
        transaction {
            val actualError = FeatherEntity.updateWhereOrError({ FeatherTable.type eq "contour" }) {
                type = "empty"
            }.assertLeft()

            assertEquals(NotFoundError::class, actualError::class)
        }
    }
}
