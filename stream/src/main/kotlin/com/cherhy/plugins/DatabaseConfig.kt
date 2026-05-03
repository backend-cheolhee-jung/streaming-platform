package com.cherhy.plugins

import com.cherhy.util.ApplicationConfigUtils.getDataSource
import com.cherhy.util.property.DataSourceProperty.DRIVER_CLASS_NAME
import com.cherhy.util.property.DataSourceProperty.MAX_POOL_SIZE
import com.cherhy.util.property.DataSourceProperty.PASSWORD
import com.cherhy.util.property.DataSourceProperty.TRANSACTION_ISOLATION
import com.cherhy.util.property.DataSourceProperty.URL
import com.cherhy.util.property.DataSourceProperty.USERNAME
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.database.TransactionIsolation

val database = Database.connect(
    HikariDataSource(
        HikariConfig().apply {
            driverClassName = getDataSource(DRIVER_CLASS_NAME)
            jdbcUrl = getDataSource(URL)
            username = getDataSource(USERNAME)
            password = getDataSource(PASSWORD)
            maximumPoolSize = getDataSource(MAX_POOL_SIZE).toInt()
            transactionIsolation = TRANSACTION_ISOLATION
            isAutoCommit = false
        }
    )
)

val transactionManager = database.transactionManager
suspend fun <T> reactiveTransaction(
    isolation: TransactionIsolation = TransactionIsolation.READ_COMMITTED,
    block: suspend () -> T,
) =
    withContext(IO) {
        val transaction = transactionManager.newTransaction(isolation)
        transaction.connection.use {
            block.invoke()
        }
    }
