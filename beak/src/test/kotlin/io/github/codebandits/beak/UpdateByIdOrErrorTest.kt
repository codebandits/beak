package io.github.codebandits.beak

import org.jetbrains.exposed.dao.EntityID
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
        transaction {
            val expectedEntity = FeatherEntity.newOrError {
                type = "contour"
            }.assertRight()

            val id: EntityID<Long> = expectedEntity.id

            FeatherEntity.updateByIdOrError(id.value) {
                type = "contour-updated"
            }.assertRight()

            assertEquals(
                "contour-updated",
                FeatherEntity.findByIdOrError(id.value).assertRight().type
            )
        }
    }

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
            val actualError: DataAccessError = FeatherEntity.updateByIdOrError(0L) {
                type = "should not happen"
            }.assertLeft()

            assertEquals(DataAccessError.SystemError.ConnectionError::class, actualError::class)
        }
    }

    @Test
    fun `should return a failure when there is no entities to update`() {
        val actualError: DataAccessError = FeatherEntity.updateByIdOrError(0L) { type = "error" }.assertLeft()

        assertEquals(DataAccessError.SystemError.TransactionError::class, actualError::class)
    }
}
