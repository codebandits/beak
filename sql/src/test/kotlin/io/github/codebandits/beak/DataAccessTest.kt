package io.github.codebandits.beak

import arrow.core.getOrElse
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

open class DataAccessTest {

    @BeforeEach
    open fun setUp() {
        Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
    }

    @Test
    fun `allOrError should return a record for each item in the database`() {
        transaction {
            create(FeatherTable)
            FeatherTable.insert {}

            assertEquals(1, FeatherEntity.allOrError().getOrElse { fail() }.count())
        }
    }
}
