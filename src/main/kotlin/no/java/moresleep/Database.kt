package no.java.moresleep

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import java.sql.Connection
import javax.sql.DataSource

enum class DataBaseType(val driverClass:String?=null) {
    POSTGRES,SQLLITE,PGINMEM("no.anksoft.pginmem.PgInMemDatasource");

    fun jdbcUrl(dbHost:String, dbPort:String, dataSourceName:String) = when (this) {
        POSTGRES -> "jdbc:postgresql://$dbHost:$dbPort/$dataSourceName"
        SQLLITE -> "jdbc:sqlite::memory:"
        PGINMEM -> "jdbc:pginmem::memory:"
    }
}

object Database {
    val datasource:DataSource by lazy {
        val dbHost = Setup.readValue(SetupValue.DBHOST)
        val dbPort = Setup.readValue(SetupValue.DBPORT)
        val dataSourceName = Setup.readValue(SetupValue.DATASOURCENAME)
        val dbUser = Setup.readValue(SetupValue.DBUSER)
        val dbPassword = Setup.readValue(SetupValue.DBPASSWORD)

        val dataBaseType:DataBaseType = DataBaseType.valueOf(Setup.readValue(SetupValue.DATABASE_TYPE))

        if (dataBaseType == DataBaseType.PGINMEM) {
            Class.forName("no.anksoft.pginmem.PgInMemDatasource").getDeclaredConstructor().newInstance() as DataSource
        } else {

            val hikariConfig = HikariConfig()
            if (dataBaseType.driverClass != null) {
                hikariConfig.dataSourceClassName = dataBaseType.driverClass
            }
            hikariConfig.jdbcUrl = dataBaseType.jdbcUrl(dbHost, dbPort, dataSourceName)
            if (dbUser.isNotEmpty()) {
                hikariConfig.username = dbUser
                hikariConfig.password = dbPassword
            }
            hikariConfig.maximumPoolSize = 10
            val ds = HikariDataSource(hikariConfig)
            ds
        }
    }

    fun migrateWithFlyway(spesialSetup:((Flyway) -> Unit)?=null, preset:((Connection)->Unit)?=null) {
        if (!Setup.readBoolValue(SetupValue.DO_FLYWAY_MIGRATION)) {
            return
        }
        if (preset != null) {
            connection().use {
                preset.invoke(it)
            }
        }
        val flyway:Flyway = Flyway.configure().dataSource(datasource).load()
        spesialSetup?.invoke(flyway)
        flyway.migrate()
    }

    fun connection():Connection {
        val conn = datasource.connection
        conn.autoCommit = false
        return conn
    }

}