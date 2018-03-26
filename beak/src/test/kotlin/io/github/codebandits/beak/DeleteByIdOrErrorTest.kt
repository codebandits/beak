package io.github.codebandits.beak

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class DeleteByIdOrErrorH2Test : DeleteByIdOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class DeleteByIdOrErrorMysqlTest : DeleteByIdOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class DeleteByIdOrErrorPostgresqlTest : DeleteByIdOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class DeleteByIdOrErrorTest : TestWithDatabase() {

    @Test
    fun `should delete an entity from the database`() {
        val featherId = FeatherEntity.newOrError {}.assertRight().id.value
        assertEquals(1, FeatherEntity.countOrError().assertRight())

        FeatherEntity.deleteByIdOrError(featherId).assertRight()
        assertEquals(0, FeatherEntity.countOrError().assertRight())
    }

    @Test
    fun `should return not found error when entity does not exist`() {
        val error = FeatherEntity.deleteByIdOrError(0L).assertLeft()
        assertEquals(DataAccessError.QueryError.NotFoundError::class, error::class)
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        val error = FeatherEntity.deleteByIdOrError(0L).assertLeft()
        assertEquals(DataAccessError.SystemError.ConnectionError::class, error::class)
    }
}
