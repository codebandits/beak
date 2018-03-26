package io.github.codebandits.beak

import io.github.codebandits.beak.DataAccessError.SystemError.ConnectionError
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class FindWhereOrErrorH2Test : FindWhereOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class FindWhereOrErrorMysqlTest : FindWhereOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class FindWhereOrErrorPostgresqlTest : FindWhereOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class FindWhereOrErrorTest : TestWithDatabase() {

    @Test
    fun `should return each matching record in the database`() {
        FeatherEntity.newOrError { type = "contour" }
        FeatherEntity.newOrError { type = "contour" }
        FeatherEntity.newOrError { type = "down" }

        assertEquals(2, FeatherEntity.findWhereOrError { FeatherTable.type eq "contour" }.assertRight().count())
    }

    @Test
    fun `should return an empty list when no records match`() {
        FeatherEntity.newOrError { type = "contour" }
        FeatherEntity.newOrError { type = "contour" }
        FeatherEntity.newOrError { type = "down" }

        assertEquals(0, FeatherEntity.findWhereOrError { FeatherTable.type eq "filoplume" }.assertRight().count())
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        val error = FeatherEntity.findWhereOrError { FeatherTable.type eq "contour" }.assertLeft()
        assertEquals(ConnectionError::class, error::class)
    }
}
