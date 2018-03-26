package io.github.codebandits.beak

import io.github.codebandits.beak.DataAccessError.QueryError.NotFoundError
import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class FindByIdOrErrorH2Test : FindByIdOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class FindByIdOrErrorMysqlTest : FindByIdOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class FindByIdOrErrorPostgresqlTest : FindByIdOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class FindByIdOrErrorTest : TestWithDatabase() {

    @Test
    fun `should retrieve an entity from the database`() {
        val featherId = FeatherEntity.newOrError { type = "contour" }.assertRight().id.value
        assertEquals(featherId, FeatherEntity.findByIdOrError(featherId).assertRight().id.value)
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        val error = FeatherEntity.findByIdOrError(0L).assertLeft()
        assertEquals(ConnectionError::class, error::class)
    }

    @Test
    fun `should return not found error when entity does not exist`() {
        val error = FeatherEntity.findByIdOrError(0L).assertLeft()

        assertEquals(NotFoundError::class, error::class)
        assertEquals(NoSuchElementException::class, error.cause::class)
    }
}
