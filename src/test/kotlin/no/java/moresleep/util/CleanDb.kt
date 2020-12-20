package no.java.moresleep.util

import no.java.moresleep.Database
import org.flywaydb.core.Flyway

class CleanDb {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val source = Database.datasource
            val flyway = Flyway.configure().dataSource(source).load()
            flyway.clean()
        }
    }
}