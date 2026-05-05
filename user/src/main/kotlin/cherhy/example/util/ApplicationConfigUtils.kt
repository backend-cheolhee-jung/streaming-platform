package cherhy.example.util

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

object ApplicationConfigUtils {
    fun getDataSource(
        dataSourceType: DataSourceType,
        key: String,
    ) = HoconApplicationConfig(ConfigFactory.load())
        .property("database.${dataSourceType.name.lowercase()}.datasource.$key")
        .getString()

    fun getJwt(
        key: String,
    ) = HoconApplicationConfig(ConfigFactory.load())
        .property("jwt.$key")
        .getString()
}
