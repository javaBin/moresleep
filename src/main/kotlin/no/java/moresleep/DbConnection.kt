package no.java.moresleep

import java.sql.Connection
import java.sql.PreparedStatement

interface DbConnection:AutoCloseable {
    fun preparedStatement(sql:String,dbcommand:(PreparedStatement)->Unit)
}