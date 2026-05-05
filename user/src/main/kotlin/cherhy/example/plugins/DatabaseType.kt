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
        connectionFactory = ConnectionPool(
            ConnectionPoolConfiguration.builder(
                ConnectionFactories.get(
                    ConnectionFactoryOptions.parse(getDataSource(MASTER, URL))
                        .mutate()
                        .option(ConnectionFactoryOptions.USER, getDataSource(MASTER, USERNAME))
                        .option(ConnectionFactoryOptions.PASSWORD, getDataSource(MASTER, PASSWORD))
                        .build()
                )
            )
                .maxSize(getDataSource(MASTER, MAX_POOL_SIZE).toInt())
                .build()
        ),
    )
    DatabaseFactory.slaveDatabase = R2dbcDatabase.connect(
        connectionFactory = ConnectionPool(
            ConnectionPoolConfiguration.builder(
                ConnectionFactories.get(
                    ConnectionFactoryOptions.parse(getDataSource(SLAVE, URL))
                        .mutate()
                        .option(ConnectionFactoryOptions.USER, getDataSource(SLAVE, USERNAME))
                        .option(ConnectionFactoryOptions.PASSWORD, getDataSource(SLAVE, PASSWORD))
                        .build()
                )
            )
                .maxSize(getDataSource(SLAVE, MAX_POOL_SIZE).toInt())
                .build()
        ),
    )
}
