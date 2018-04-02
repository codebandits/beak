package io.github.codebandits.beak

import io.github.codebandits.beak.DataAccessError.QueryError.BadRequestError
import io.github.codebandits.beak.DataAccessError.QueryError.NotFoundError
import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
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
        val featherId = FeatherEntity.newOrError { type = "contour" }.assertRight().id.value
        FeatherEntity.updateByIdOrError(featherId) { type = "down" }.assertRight()

        assertEquals("down", FeatherEntity.findByIdOrError(featherId).assertRight().type)
    }

    @Test
    fun `should return a failure when the entity does not exist`() {
        val error = FeatherEntity.updateByIdOrError(0L) { type = "down" }.assertLeft()
        assertEquals(NotFoundError::class, error::class)
    }

    @Test
    fun `should return a failure when the data is invalid`() {
        val featherId = FeatherEntity.newOrError { type = "contour" }.assertRight().id.value
        val error = FeatherEntity.updateByIdOrError(featherId) { type = "x".repeat(500) }.assertLeft()
        assertEquals(BadRequestError::class, error::class)
    }

    @Test
    fun `should be able to access referrers`() {
        val bird = BirdEntity.newOrError { name = "Steve" }.assertRight()

        FeatherEntity.newOrError {
            type = "down"
            this.bird = bird.id
        }.assertRight()

        FeatherEntity.newOrError {
            type = "filoplume"
            this.bird = bird.id
        }.assertRight()

        assertEquals(
            listOf("down", "filoplume"),
            BirdEntity.updateByIdOrError(bird.id.value) { name = "Bald Eagle" }
                .assertRight()
                .feathers
                .map { it.type }
        )
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        val error = FeatherEntity.updateByIdOrError(0L) { type = "down" }.assertLeft()
        assertEquals(ConnectionError::class, error::class)
    }
}
