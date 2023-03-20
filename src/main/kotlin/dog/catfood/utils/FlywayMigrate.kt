package dog.catfood.utils

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.commandLineEnvironment
import org.flywaydb.core.Flyway

object FlywayMigrate {
    fun main(args: Array<String>, configPath: String = "db.migration") {
        val config = commandLineEnvironment(args).config.config(configPath)
        migrate(config)
    }

    fun migrate(config: ApplicationConfig) {
        Flyway.configure()
            .dataSource(
                config.get("url"),
                config.get("user"),
                config.get("password")
            )
            .load()
            .migrate()
    }
}