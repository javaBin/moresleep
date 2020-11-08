package no.java.moresleep

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

interface DbConnection:AutoCloseable {
    fun preparedStatement(sql:String,dbcommand:(PreparedStatement)->Unit)
    fun <T> allFromQuery(sql:String,dbcommand: (ResultSet)->T):List<T>
}

fun ResultSet.requiredString(name:String):String =
    this.getString(name)?:throw RuntimeException("NotNullable column $name was null")