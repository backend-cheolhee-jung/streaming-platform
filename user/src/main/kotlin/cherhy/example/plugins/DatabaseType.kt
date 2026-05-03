package cherhy.example.plugins

import cherhy.example.util.ApplicationConfigUtils.getDataSource
import cherhy.example.util.DataSourceType.MASTER
import cherhy.example.util.DataSourceType.SLAVE
import cherhy.example.util.DatabaseFactory
import cherhy.example.util.property.DataSourceProperty.PASSWORD
import cherhy.example.util.property.DataSourceProperty.URL
import cherhy.example.util.property.DataSourceProperty.USERNAME
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

fun Application.configureDatabase() {
    DatabaseFactory.masterDb = R2dbcDatabase.connect(
        url = getDataSource(MASTER, URL),
        user = getDataSource(MASTER, USERNAME),
        password = getDataSource(MASTER, PASSWORD),
    )
    DatabaseFactory.slaveDb = R2dbcDatabase.connect(
        url = getDataSource(SLAVE, URL),
        user = getDataSource(SLAVE, USERNAME),
        password = getDataSource(SLAVE, PASSWORD),
    )
}
