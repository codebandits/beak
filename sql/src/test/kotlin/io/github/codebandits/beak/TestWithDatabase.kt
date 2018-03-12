package io.github.codebandits.beak

import org.h2.tools.Server
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class TestWithDatabase {

    protected lateinit var server: Server

    @BeforeEach
    internal fun setUp() {
        server = Server.createTcpServer().start()
        Database.connect(
                url = "jdbc:h2:${server.url}/mem:test;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver"
        )

        transaction {
            SchemaUtils.create(FeatherTable)
        }
    }

    @AfterEach
    fun tearDown() {
        if (!server.isRunning(false)) server.start()
        transaction { exec("DROP ALL OBJECTS;") }
        server.stop()
    }
}