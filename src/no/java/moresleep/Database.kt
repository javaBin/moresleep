package no.java.moresleep

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

object Database {
    private val datasource:DataSource by lazy {
        val dbHost = Setup.readValue(SetupValue.DBHOST)
        val dbPort = Setup.readValue(SetupValue.DBPORT)
        val dataSourceName = Setup.readValue(SetupValue.DATASOURCENAME)
        val dbUser = Setup.readValue(SetupValue.DBUSER)
        val dbPassword = Setup.readValue(SetupValue.DBPASSWORD)

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dataSourceName"
        hikariConfig.username = dbUser
        hikariConfig.password = dbPassword
        hikariConfig.maximumPoolSize = 10
        val ds = HikariDataSource(hikariConfig)
        ds
    }

    private fun migrateWithFlyway(spesialSetup:((Flyway) -> Unit)?) {
        val flyway:Flyway = Flyway.configure().dataSource(datasource).load()
        spesialSetup?.invoke(flyway)
        flyway.migrate()
    }

}