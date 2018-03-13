package io.github.codebandits.beak

import arrow.core.Either
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FindEntityTest : TestWithDatabase() {

    @Test
    fun `should retrieve an entity from the database`() {
        transaction {
            val expectedEntity = FeatherEntity.newOrError {
                type = "contour"
            }.assertRight()

            val id: EntityID<Long> = expectedEntity.id
            assertEquals(
                Either.right(expectedEntity).assertRight(),
                FeatherEntity.findByIdOrError(id.value).assertRight()
            )
        }
    }

    @Test
    fun `should retrieve a system error when server is down`() {
        server.stop()

        transaction {
            val actualError: DataAccessError = FeatherEntity.findByIdOrError(0L).assertLeft()

            assertEquals(DataAccessError.SystemError.ConnectionError::class, actualError::class)
        }
    }

    @Test
    fun `should return not found error when entity does not exist`() {
        transaction {
            val fakeId = 0L
            val actualError: DataAccessError = FeatherEntity.findByIdOrError(fakeId).assertLeft()

            assertEquals(DataAccessError.EntityError.NotFoundError::class, actualError::class)
        }
    }
}