package cherhy.example.plugins

import cherhy.example.util.ApplicationConfigUtils.getDataSource
import cherhy.example.util.DataSourceType.MASTER
import cherhy.example.util.DataSourceType.SLAVE
import cherhy.example.util.DatabaseFactory
import cherhy.example.util.property.DataSourceProperty.MAX_POOL_SIZE
import cherhy.example.util.property.DataSourceProperty.PASSWORD
import cherhy.example.util.property.DataSourceProperty.URL
import cherhy.example.util.property.DataSourceProperty.USERNAME
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

fun Application.configureDatabase() {
    DatabaseFactory.masterDatabase = R2dbcDatabase.connect(
        connectionFactory = buildConnectionPool(MASTER),
    )
    DatabaseFactory.slaveDatabase = R2dbcDatabase.connect(
        connectionFactory = buildConnectionPool(SLAVE),
    )
}

private fun buildConnectionPool(dataSourceType: cherhy.example.util.DataSourceType): ConnectionPool {
    val url = getDataSource(dataSourceType, URL)
    val user = getDataSource(dataSourceType, USERNAME)
    val password = getDataSource(dataSourceType, PASSWORD)
    val maxPoolSize = getDataSource(dataSourceType, MAX_POOL_SIZE).toInt()

    val connectionFactory = ConnectionFactories.get(
        ConnectionFactoryOptions.parse(url)
            .mutate()
            .option(ConnectionFactoryOptions.USER, user)
            .option(ConnectionFactoryOptions.PASSWORD, password)
            .build()
    )

    val poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxSize(maxPoolSize)
        .build()

    return ConnectionPool(poolConfig)
}
