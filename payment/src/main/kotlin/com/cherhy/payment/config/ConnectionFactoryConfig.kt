package com.cherhy.payment.config

import com.cherhy.payment.util.constant.DataSource.MASTER_DATASOURCE
import com.cherhy.payment.util.constant.DataSource.SLAVE_DATASOURCE
import com.cherhy.payment.util.property.R2dbcDataSourceProperty
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableR2dbcAuditing
class ConnectionFactoryConfig(
    private val r2dbcProperty: R2dbcDataSourceProperty,
) {
    @Bean(MASTER_CONNECTION_FACTORY)
    fun masterConnectionFactory(): ConnectionFactory =
        ConnectionFactoryBuilder.withUrl(r2dbcProperty.master.url)
            .username(r2dbcProperty.master.username)
            .password(r2dbcProperty.master.password)
            .build()

    @Bean(SLAVE_CONNECTION_FACTORY)
    fun slaveConnectionFactory(): ConnectionFactory =
        ConnectionFactoryBuilder.withUrl(r2dbcProperty.slave.url)
            .username(r2dbcProperty.slave.username)
            .password(r2dbcProperty.slave.password)
            .build()

    @Bean
    @Primary
    @DependsOn(MASTER_CONNECTION_FACTORY, SLAVE_CONNECTION_FACTORY)
    fun connectionFactory(
        @Qualifier(MASTER_CONNECTION_FACTORY) masterConnectionFactory: ConnectionFactory,
        @Qualifier(SLAVE_CONNECTION_FACTORY) slaveConnectionFactory: ConnectionFactory,
    ): ConnectionFactory {
        val factories = mapOf(
            MASTER_DATASOURCE to masterConnectionFactory,
            SLAVE_DATASOURCE to slaveConnectionFactory,
        )

        return R2dbcRoutingDataSource().apply {
            setTargetConnectionFactories(factories)
            setDefaultTargetConnectionFactory(masterConnectionFactory)
            afterPropertiesSet()
        }
    }

    @Bean
    @DependsOn(CONNECTION_FACTORY)
    fun reactiveTransactionManager(
        @Qualifier(CONNECTION_FACTORY) connectionFactory: ConnectionFactory,
    ): ReactiveTransactionManager =
        R2dbcTransactionManager(connectionFactory)

    @Bean
    @DependsOn(CONNECTION_FACTORY)
    fun databaseClient(
        @Qualifier(CONNECTION_FACTORY) connectionFactory: ConnectionFactory,
    ) =
        DatabaseClient.create(connectionFactory)

    @Bean
    @DependsOn(CONNECTION_FACTORY)
    fun r2dbcEntityTemplate(
        @Qualifier(CONNECTION_FACTORY) connectionFactory: ConnectionFactory
    ) =
        R2dbcEntityTemplate(DatabaseClient.create(connectionFactory), PostgresDialect.INSTANCE)

    @Bean
    @DependsOn(CONNECTION_FACTORY)
    fun transactionalOperator(
        @Qualifier(CONNECTION_FACTORY) connectionFactory: ConnectionFactory,
    ) =
        TransactionalOperator.create(
            reactiveTransactionManager(connectionFactory)
        )

    companion object {
        const val CONNECTION_FACTORY = "connectionFactory"
        const val MASTER_CONNECTION_FACTORY = "masterConnectionFactory"
        const val SLAVE_CONNECTION_FACTORY = "slaveConnectionFactory"
    }
}
