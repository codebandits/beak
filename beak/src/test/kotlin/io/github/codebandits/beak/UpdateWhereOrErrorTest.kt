package io.github.codebandits.beak

import io.github.codebandits.beak.DataAccessError.QueryError.NotFoundError
import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
        FeatherEntity.newOrError { type = "down" }.assertRight()
        FeatherEntity.newOrError { type = "contour" }.assertRight()
        FeatherEntity.newOrError { type = "contour" }.assertRight()

        FeatherEntity.updateWhereOrError({ FeatherTable.type eq "contour" }) {
            type = "filoplume"
        }.assertRight()

        assertEquals(2, FeatherEntity.countOrError(FeatherTable.type eq "filoplume").assertRight())
    }

    @Test
    fun `should return a failure when there are no entities to update`() {
        val error = FeatherEntity.updateWhereOrError({ FeatherTable.type eq "contour" }) {
            type = "down"
        }.assertLeft()
        assertEquals(NotFoundError::class, error::class)
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        val error = FeatherEntity.updateWhereOrError({ FeatherTable.id eq 0L }) { type = "down" }.assertLeft()
        assertEquals(ConnectionError::class, error::class)
    }
}
