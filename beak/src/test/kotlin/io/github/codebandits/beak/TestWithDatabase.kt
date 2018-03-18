package io.github.codebandits.beak

import org.h2.tools.Server
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.netcrusher.tcp.TcpCrusherBuilder
import java.sql.DriverManager
import org.netcrusher.core.reactor.NioReactor


abstract class TestWithDatabase {

    abstract val databaseConfiguration: DatabaseConfiguration

    @BeforeEach
    fun setUp() {
        databaseConfiguration.setUp()
    }

    @AfterEach
    fun tearDown() {
        databaseConfiguration.tearDown()
    }

    data class DatabaseConfiguration(
        val setUp: () -> Unit,
        val interruptDatabase: () -> Unit,
        val tearDown: () -> Unit
    )

    protected fun h2Configuration(): DatabaseConfiguration {
        val server = Server.createTcpServer()

        return DatabaseConfiguration(
            setUp = {
                server.start()
                Database.connect(
                    url = "jdbc:h2:${server.url}/mem:test;DB_CLOSE_DELAY=-1",
                    driver = "org.h2.Driver"
                )
                transaction {
                    SchemaUtils.create(FeatherTable)
                }
            },
            interruptDatabase = {
                server.stop()
            },
            tearDown = {
                if (!server.isRunning(false)) server.start()
                transaction { SchemaUtils.drop(FeatherTable) }
                server.stop()
            }
        )
    }

    protected fun mysqlConfiguration(): DatabaseConfiguration {
        val rootConnection =
            DriverManager.getConnection("jdbc:mysql://localhost/?user=root&password=&serverTimezone=UTC&useSSL=false")

        val proxy = TcpCrusherBuilder.builder()
            .withReactor(NioReactor())
            .withBindAddress("localhost", 3307)
            .withConnectAddress("localhost", 3306)
            .build()

        return DatabaseConfiguration(
            setUp = {
                rootConnection.createStatement().execute("DROP DATABASE IF EXISTS beaktest")
                rootConnection.createStatement().execute("CREATE DATABASE beaktest")
                rootConnection.createStatement()
                    .execute("GRANT ALL PRIVILEGES ON beaktest.* TO 'beaktest' IDENTIFIED BY 'beaktest'")

                proxy.open()

                Database.connect(
                    url = "jdbc:mysql://beaktest:beaktest@localhost:3307/beaktest?serverTimezone=UTC&useSSL=false",
                    driver = "com.mysql.cj.jdbc.Driver"
                )
                transaction {
                    SchemaUtils.create(FeatherTable)
                }
            },
            interruptDatabase = {
                proxy.close()
            },
            tearDown = {
                if (!proxy.isOpen) proxy.open()
                transaction { SchemaUtils.drop(FeatherTable) }
                proxy.close()
            }
        )
    }
}
