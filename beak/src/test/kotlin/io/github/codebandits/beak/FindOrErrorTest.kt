package io.github.codebandits.beak

import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class FindOrErrorH2Test : FindOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class FindOrErrorMysqlTest : FindOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class FindOrErrorPostgresqlTest : FindOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class FindOrErrorTest : TestWithDatabase() {

    @Test
    fun `should return each matching record in the database`() {
        transaction {
            FeatherEntity.newOrError { type = "contour" }
            FeatherEntity.newOrError { type = "contour" }
            FeatherEntity.newOrError { type = "down" }
        }

        transaction {
            assertEquals(2, FeatherEntity.findOrError { FeatherTable.type eq "contour" }.assertRight().count())
        }
    }

    @Test
    fun `should return an empty list when no records match`() {
        transaction {
            FeatherEntity.newOrError { type = "contour" }
            FeatherEntity.newOrError { type = "contour" }
            FeatherEntity.newOrError { type = "down" }
        }

        transaction {
            assertEquals(0, FeatherEntity.findOrError { FeatherTable.type eq "filoplume" }.assertRight().count())
        }
    }

    @Test
    fun `should return a failure when the database cannot connect`() {
        databaseConfiguration.interruptDatabase()

        transaction {
            val actualError: DataAccessError = FeatherEntity.findOrError { FeatherTable.type eq "contour" }.assertLeft()

            assertEquals(DataAccessError.SystemError.ConnectionError::class, actualError::class)
        }
    }

    @Test
    fun `should return a failure when there is no transaction`() {
        val actualError: DataAccessError = FeatherEntity.findOrError { FeatherTable.type eq "contour" }.assertLeft()

        assertEquals(DataAccessError.SystemError.TransactionError::class, actualError::class)
    }
}
