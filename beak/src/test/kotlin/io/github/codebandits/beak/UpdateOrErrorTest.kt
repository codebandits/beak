package io.github.codebandits.beak

import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class UpdateOrErrorH2Test : UpdateOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class UpdateOrErrorMysqlTest : UpdateOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class UpdateOrErrorPostgresqlTest : UpdateOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class UpdateOrErrorTest : TestWithDatabase() {

    @Test
    fun `should update multiple entities that returns from query from the table`() {
        transaction {
            FeatherEntity.newOrError {
                type = "contour"
            }.assertRight()

            FeatherEntity.newOrError {
                type = "contour"
            }.assertRight()

            FeatherEntity.updateOrError({ FeatherTable.type eq "contour" }) {
                type = "contour-updated"
            }.assertRight()

            assertEquals(
                2,
                FeatherEntity.findOrError({ FeatherTable.type eq "contour-updated" }).assertRight().count()
            )
        }
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        transaction {
            val actualError: DataAccessError = FeatherEntity.updateOrError({ FeatherTable.id eq 0L }) {
                type = "should not happen"
            }.assertLeft()

            assertEquals(DataAccessError.SystemError.ConnectionError::class, actualError::class)
        }
    }

    @Test
    fun `should return a failure when there is no entities to update`() {
        val actualError: DataAccessError = FeatherEntity.updateOrError({ FeatherTable.type eq "contour" }) {
            type = "empty"
        }.assertLeft()

        assertEquals(DataAccessError.SystemError.TransactionError::class, actualError::class)
    }
}
