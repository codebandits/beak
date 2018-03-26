package io.github.codebandits.beak

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("h2")
class CountOrErrorH2Test : CountOrErrorTest() {
    override val databaseConfiguration = h2Configuration()
}

@Tag("mysql")
class CountOrErrorMysqlTest : CountOrErrorTest() {
    override val databaseConfiguration = mysqlConfiguration()
}

@Tag("postgresql")
class CountOrErrorPostgresqlTest : CountOrErrorTest() {
    override val databaseConfiguration = postgresqlConfiguration()
}

abstract class CountOrErrorTest : TestWithDatabase() {

    @Test
    fun `should return 0 when there are no entities`() {
        assertEquals(0, FeatherEntity.countOrError().assertRight())
    }

    @Test
    fun `should return 3 when there are 3 entities`() {
        repeat(3) {
            FeatherEntity.newOrError { }.assertRight()
        }

        assertEquals(3, FeatherEntity.countOrError().assertRight())
    }

    @Test
    fun `should return the count for a given statement`() {
        FeatherEntity.newOrError { type = "down" }.assertRight()
        FeatherEntity.newOrError { type = "filoplume" }.assertRight()
        FeatherEntity.newOrError { type = "filoplume" }.assertRight()

        assertEquals(1, FeatherEntity.countOrError(FeatherTable.type eq "down").assertRight())
    }
}
