package io.github.codebandits.beak

import io.github.codebandits.beak.DataAccessError.QueryError.MultipleFoundError
import io.github.codebandits.beak.DataAccessError.QueryError.NotFoundError
import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class FindOneWhereOrErrorH2Test : FindOneWhereOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class FindOneWhereOrErrorMysqlTest : FindOneWhereOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class FindOneWhereOrErrorPostgresqlTest : FindOneWhereOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class FindOneWhereOrErrorTest : TestWithDatabase() {

    @Test
    fun `should return the matching record in the database`() {
        FeatherEntity.newOrError { type = "contour" }.assertRight()
        FeatherEntity.newOrError { type = "contour" }.assertRight()
        FeatherEntity.newOrError { type = "down" }.assertRight()

        val featherEntity = FeatherEntity.findOneWhereOrError { FeatherTable.type eq "down" }.assertRight()
        assertEquals("down", featherEntity.type)
    }

    @Test
    fun `should return a failure when no records match`() {
        FeatherEntity.newOrError { type = "contour" }.assertRight()
        FeatherEntity.newOrError { type = "contour" }.assertRight()
        FeatherEntity.newOrError { type = "down" }.assertRight()

        val error = FeatherEntity.findOneWhereOrError { FeatherTable.type eq "filoplume" }.assertLeft()
        assertEquals(NotFoundError::class, error::class)
    }

    @Test
    fun `should return a failure when multiple records match`() {
        FeatherEntity.newOrError { type = "contour" }.assertRight()
        FeatherEntity.newOrError { type = "contour" }.assertRight()
        FeatherEntity.newOrError { type = "down" }.assertRight()

        val error = FeatherEntity.findOneWhereOrError { FeatherTable.type eq "contour" }.assertLeft()
        assertEquals(MultipleFoundError::class, error::class)
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        val error = FeatherEntity.findOneWhereOrError { FeatherTable.type eq "contour" }.assertLeft()
        assertEquals(ConnectionError::class, error::class)
    }
}
