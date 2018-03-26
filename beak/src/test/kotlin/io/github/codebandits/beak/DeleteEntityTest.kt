package io.github.codebandits.beak

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class DeleteEntityH2Test : DeleteEntityTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class DeleteEntityMysqlTest : DeleteEntityTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class DeleteEntityPostgresqlTest : DeleteEntityTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class DeleteEntityTest : TestWithDatabase() {

    @Test
    fun `should delete an entity from the database`() {
        val featherId = FeatherEntity.newOrError {}.assertRight().id.value
        assertEquals(1, FeatherEntity.countOrError().assertRight())

        FeatherEntity.deleteOrError(featherId).assertRight()
        assertEquals(0, FeatherEntity.countOrError().assertRight())
    }

    @Test
    fun `should return not found error when entity does not exist`() {
        val error = FeatherEntity.deleteOrError(0L).assertLeft()
        assertEquals(DataAccessError.QueryError.NotFoundError::class, error::class)
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        val error = FeatherEntity.deleteOrError(0L).assertLeft()
        assertEquals(DataAccessError.SystemError.ConnectionError::class, error::class)
    }
}
