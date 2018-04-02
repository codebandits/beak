package io.github.codebandits.beak

import org.apache.http.client.utils.URIBuilder
import org.h2.tools.Server
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.netcrusher.core.reactor.NioReactor
import org.netcrusher.tcp.TcpCrusherBuilder
import java.net.ServerSocket
import java.net.URI
import java.sql.DriverManager

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
                    SchemaUtils.create(BirdTable, FeatherTable)
                }
            },
            interruptDatabase = {
                server.stop()
            },
            tearDown = {
                if (!server.isRunning(false)) server.start()
                transaction { SchemaUtils.drop(BirdTable, FeatherTable) }
                server.stop()
            }
        )
    }

    protected fun mysqlConfiguration(): DatabaseConfiguration {

        val localMysqlHost = "mysql://root:@localhost:3306/?serverTimezone=UTC&useSSL=false"

        val rootHostUri = (System.getProperty("test.db.mysql") ?: localMysqlHost).let(URI::create)

        val proxyHostUri = URIBuilder(rootHostUri)
            .setHost("localhost")
            .setPort(findOpenPort())
            .setPath("/beaktest")
            .setUserInfo("beaktest:beaktest")
            .build()

        val rootConnection = DriverManager.getConnection("jdbc:$rootHostUri")

        val proxy = TcpCrusherBuilder.builder()
            .withReactor(NioReactor())
            .withBindAddress(proxyHostUri.host, proxyHostUri.port)
            .withConnectAddress(rootHostUri.host, rootHostUri.port)
            .build()

        return DatabaseConfiguration(
            setUp = {
                rootConnection.createStatement().execute("DROP DATABASE IF EXISTS beaktest")
                rootConnection.createStatement().execute("CREATE DATABASE beaktest")
                rootConnection.createStatement()
                    .execute("GRANT ALL PRIVILEGES ON beaktest.* TO 'beaktest' IDENTIFIED BY 'beaktest'")

                proxy.open()

                Database.connect(url = "jdbc:$proxyHostUri", driver = "com.mysql.cj.jdbc.Driver")

                transaction {
                    SchemaUtils.create(BirdTable, FeatherTable)
                }
            },
            interruptDatabase = {
                proxy.close()
            },
            tearDown = {
                if (!proxy.isOpen) proxy.open()
                transaction { SchemaUtils.drop(BirdTable, FeatherTable) }
                proxy.close()
            }
        )
    }

    protected fun postgresqlConfiguration(): DatabaseConfiguration {

        val localPostgresqlHost = "postgresql://localhost:5432/postgres"

        val rootHostUri = (System.getProperty("test.db.postgresql") ?: localPostgresqlHost).let(URI::create)

        val proxyHostUri = URIBuilder(rootHostUri)
            .setHost("localhost")
            .setPort(findOpenPort())
            .setPath("/beaktest")
            .setParameter("user", "beaktest")
            .setParameter("password", "beaktest")
            .build()

        val rootConnection = DriverManager.getConnection("jdbc:$rootHostUri")

        val proxy = TcpCrusherBuilder.builder()
            .withReactor(NioReactor())
            .withBindAddress(proxyHostUri.host, proxyHostUri.port)
            .withConnectAddress(rootHostUri.host, rootHostUri.port)
            .build()

        return DatabaseConfiguration(
            setUp = {
                rootConnection.createStatement().execute("DROP DATABASE IF EXISTS beaktest")
                rootConnection.createStatement().execute("DROP USER IF EXISTS beaktest")
                rootConnection.createStatement().execute("CREATE DATABASE beaktest")
                rootConnection.createStatement().execute("CREATE USER beaktest WITH PASSWORD 'beaktest'")
                rootConnection.createStatement()
                    .execute("GRANT ALL PRIVILEGES ON DATABASE beaktest TO beaktest")

                proxy.open()

                Database.connect(url = "jdbc:$proxyHostUri", driver = "org.postgresql.Driver")

                transaction {
                    SchemaUtils.create(BirdTable, FeatherTable)
                }
            },
            interruptDatabase = {
                proxy.close()
            },
            tearDown = {
                if (!proxy.isOpen) proxy.open()
                transaction { SchemaUtils.drop(BirdTable, FeatherTable) }
                proxy.close()
            }
        )
    }

    private fun findOpenPort(): Int {
        val socket = ServerSocket(0)
        socket.close()
        return socket.localPort
    }
}
