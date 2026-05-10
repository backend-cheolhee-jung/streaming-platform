package cherhy.com.plugins

import cherhy.com.util.ApplicationConfigUtils.getDataSource
import cherhy.com.util.DataSourceProperty.DRIVER_CLASS_NAME
import cherhy.com.util.DataSourceProperty.ISOLATION_LEVEL
import cherhy.com.util.DataSourceProperty.MAX_POOL_SIZE
import cherhy.com.util.DataSourceProperty.PASSWORD
import cherhy.com.util.DataSourceProperty.URL
import cherhy.com.util.DataSourceProperty.USERNAME
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDatabase() {
    val hikari = HikariDataSource(
        HikariConfig().apply {
            driverClassName = getDataSource(DRIVER_CLASS_NAME)
            jdbcUrl = getDataSource(URL)
            username = getDataSource(USERNAME)
            password = getDataSource(PASSWORD)
            maximumPoolSize = getDataSource(MAX_POOL_SIZE).toInt()
            isAutoCommit = false
            transactionIsolation = getDataSource(ISOLATION_LEVEL)
            validate()
        }
    )
    Database.connect(hikari)
}