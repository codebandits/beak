package io.github.codebandits.beak

import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("h2")
class AllOrErrorH2Test : AllOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class AllOrErrorMysqlTest : AllOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class AllOrErrorPostgresqlTest : AllOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class AllOrErrorTest : TestWithDatabase() {

    @Test
    fun `should return a record for each item in the database`() {
        FeatherEntity.newOrError { }.assertRight()

        assertEquals(1, FeatherEntity.allOrError().assertRight().count())
    }

    @Test
    fun `should be able to access the data of each record`() {
        FeatherEntity.newOrError { type = "down" }.assertRight()
        FeatherEntity.newOrError { type = "filoplume" }.assertRight()

        assertTrue {
            FeatherEntity.allOrError()
                .assertRight()
                .map { it.type }
                .containsAll(listOf("down", "filoplume"))
        }
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        val error = FeatherEntity.allOrError().assertLeft()
        assertEquals(ConnectionError::class, error::class)
    }
}
