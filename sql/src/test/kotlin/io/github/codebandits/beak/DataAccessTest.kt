package io.github.codebandits.beak

import org.h2.tools.Server
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DataAccessTest {

    private lateinit var server: Server
    private lateinit var database: Database

    @BeforeEach
    fun setUp() {
        server = Server.createTcpServer().start()
        database = Database.connect(
            url = "jdbc:h2:${server.url}/mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )

        transaction {
            create(FeatherTable)
        }
    }

    @AfterEach
    fun tearDown() {
        if (!server.isRunning(false)) server.start()
        database.connector().createStatement().execute("DROP ALL OBJECTS;")
        server.stop()
    }

    @Test
    fun `allOrError should return a record for each item in the database`() {
        transaction {
            FeatherTable.insert {}
        }

        transaction {
            assertEquals(1, FeatherEntity.allOrError().assertRight().count())
        }
    }

    @Test
    fun `allOrError should return a failure when the database cannot connect`() {
        server.stop()

        transaction {
            val actualError: DataAccessError = FeatherEntity.allOrError().assertLeft()

            assertEquals(DataAccessError.SystemError.ConnectionError::class, actualError::class)
        }
    }

    @Test
    fun `allOrError should return a failure when there is no transaction`() {
        val actualError: DataAccessError = FeatherEntity.allOrError().assertLeft()

        assertEquals(DataAccessError.SystemError.TransactionError::class, actualError::class)
    }
}
