package no.java.moresleep.util

import no.java.moresleep.ServiceExecutor
import no.java.moresleep.Setup
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

abstract class DbMigrationStep: BaseJavaMigration() {
    abstract fun doMigrate();

    override fun migrate(context: Context) {
        if (Setup.isRunningJunit) {
            return
        }
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