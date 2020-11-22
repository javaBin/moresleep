package no.java.moresleep

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime

interface DbConnection:AutoCloseable {
    fun <T> preparedStatement(sql:String,dbcommand:(PreparedStatement)->T):T
    fun <T> allFromQuery(sql:String,dbcommand: (ResultSet)->T):List<T>
}

fun <T> PreparedStatement.withResultSet(dbcommand: (ResultSet) -> T):T =
    this.executeQuery().use {
        dbcommand(it)
    }

fun <T> PreparedStatement.allFromQuery(dbCommand: (ResultSet)->T):List<T> {
    val res:MutableList<T> = mutableListOf()
    this.executeQuery().use {
        while (it.next()) {
            res.add(dbCommand(it))
        }
    }
    return res
}


fun ResultSet.requiredString(name:String):String =
    this.getString(name)?:throw MoresleepInternalError("NotNullable stringcolumn $name was null")

fun ResultSet.requiredLocalDateTime(name:String):LocalDateTime =
        this.getTimestamp(name).toLocalDateTime()?:throw MoresleepInternalError("NotNullable localdatetimecolumn $name was null")

fun PreparedStatement.setTimestamp(parameterIndex: Int, x: LocalDateTime?) = this.setTimestamp(parameterIndex, x?.let { Timestamp.valueOf(it) })
