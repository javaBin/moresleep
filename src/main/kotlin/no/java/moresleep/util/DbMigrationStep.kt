package no.java.moresleep.util

import no.java.moresleep.ServiceExecutor
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

abstract class DbMigrationStep: BaseJavaMigration() {
    abstract fun doMigrate();

    override fun migrate(context: Context) {
        val createConnection:Boolean = !ServiceExecutor.hasConnection()
        if (createConnection) {
            ServiceExecutor.createConnection {
                context.connection
            }
        }
        doMigrate()
        if (createConnection) {
            ServiceExecutor.commit()
            ServiceExecutor.removeConnection()
        }
    }
}