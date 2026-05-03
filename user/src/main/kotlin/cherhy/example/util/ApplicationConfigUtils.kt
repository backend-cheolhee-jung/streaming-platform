package cherhy.example.util

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*

object ApplicationConfigUtils {
    private fun getConfigProperty(
        path: String,
    ) =
        HoconApplicationConfig(ConfigFactory.load()).property(path).getString()

    fun getDataSource(
        dataSourceType: DataSourceType,
        key: String,
    ) = getConfigProperty("database.${dataSourceType.name.lowercase()}.datasource.$key")

    fun getJwt(
        key: String,
    ) = getConfigProperty("jwt.$key")
}