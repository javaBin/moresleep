package no.java.moresleep

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class BaseTestClass {
    @BeforeEach
    fun setup() {
        Setup.setValue(SetupValue.DATABASE_TYPE,"SQLLITE")
        Setup.setValue(SetupValue.DBUSER,"")
        Setup.setValue(SetupValue.DBPASSWORD,"")
        Setup.setValue(SetupValue.DATASOURCENAME,"junit")
        Database.migrateWithFlyway({ it.clean() })
        ServiceExecutor.createConnection()
    }

    @AfterEach
    fun teardown() {
        ServiceExecutor.closeConnection()
    }
}