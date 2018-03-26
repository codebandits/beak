package io.github.codebandits.beak

import org.jetbrains.exposed.sql.transactions.transaction
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
        transaction {
            val entity = FeatherEntity.newOrError {}.assertRight()
            assertEquals(1, FeatherEntity.allOrError().assertRight().count())

            FeatherEntity.deleteOrError(entity.id.value).assertRight()
            assertEquals(0, FeatherEntity.allOrError().assertRight().count())
        }
    }

    @Test
    fun `should return not found error when entity does not exist`() {
        transaction {
            val fakeId = 0L
            val actualError: DataAccessError = FeatherEntity.deleteOrError(fakeId).assertLeft()

            assertEquals(DataAccessError.QueryError.NotFoundError::class, actualError::class)
        }
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        transaction {
            val actualError: DataAccessError = FeatherEntity.deleteOrError(0L).assertLeft()

            assertEquals(DataAccessError.SystemError.ConnectionError::class, actualError::class)
        }
    }
}
