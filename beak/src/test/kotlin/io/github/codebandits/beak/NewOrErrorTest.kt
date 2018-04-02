package io.github.codebandits.beak

import io.github.codebandits.beak.DataAccessError.QueryError.BadRequestError
import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class NewOrErrorH2Test : NewOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class NewOrErrorMysqlTest : NewOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class NewOrErrorPostgresqlTest : NewOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class NewOrErrorTest : TestWithDatabase() {

    @Test
    fun `should create a record in the database`() {
        FeatherEntity.newOrError {}.assertRight()

        assertEquals(1, FeatherEntity.countOrError().assertRight())
    }

    @Test
    fun `should save the data into a record in the database`() {
        val featherEntity = FeatherEntity.newOrError { type = "contour" }.assertRight()
        assertEquals("contour", featherEntity.type)
    }

    @Test
    fun `should return a failure when the data is invalid`() {
        val error = FeatherEntity.newOrError { type = "x".repeat(500) }.assertLeft()
        assertEquals(BadRequestError::class, error::class)
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        val error = FeatherEntity.newOrError {}.assertLeft()
        assertEquals(ConnectionError::class, error::class)
    }
}
