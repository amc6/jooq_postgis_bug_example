package com.sharespace.persistence

import com.sharespace.database.DatabaseMigrator
import com.sharespace.database.public_.routines.TruncateAllTables
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.impl.DataSourceConnectionProvider
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.BufferedReader
import java.io.InputStreamReader

open class DBTest {

    companion object {
        private val dbSessionManager: DBSessionManager = TestDBInitializer().init()

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            dbSessionManager.startTransaction {
                TruncateAllTables().execute(it.configuration())
            }
        }
    }

    protected val dbSessionManager = DBTest.dbSessionManager

    @Suppress("unused")
    @get:Rule
    val transactionRule = TransactionRule(dbSessionManager)
}

class TransactionRule(private val dbSessionManager: DBSessionManager) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                try {
                    dbSessionManager.startTransaction {
                        try {
                            base.evaluate()
                            // Use an exception to rollback the transaction so that the db
                            // is in a clean state for the next test.
                            throw RollbackException()
                        } catch (e: Error) {
                            // This hack is needed because the transaction is not properly closed when an Error is thrown.
                            // By wrapping it in an exception the transaction will be properly rolled-back.
                            // This may not work if the error occurs inside nested transactions, but that should be
                            // uncommon in tests. See also https://github.com/jOOQ/jOOQ/issues/7167
                            throw ErrorExceptionWrapper(e)
                        }
                    }
                } catch (e: ErrorExceptionWrapper) {
                    throw e.error
                } catch (e: RollbackException) {
                    // swallow this exception.
                }
            }
        }
    }
}

private class ErrorExceptionWrapper(val error: Error) : RuntimeException()
private class RollbackException : RuntimeException()

class TestDBInitializer {

    companion object {
        const val DB_NAME = "test_runner_db_123"
        const val DB_URL = "jdbc:postgresql:$DB_NAME"

        private var initialized = false
        private lateinit var dbSessionManager: DBSessionManager
    }

    fun init(): DBSessionManager {
        if (initialized) return dbSessionManager
        initialized = true
        Runtime.getRuntime().exec(
                arrayOf(
                        "psql", "postgres", "-c", "DROP DATABASE IF EXISTS $DB_NAME;")).printErrorAndExitCode()
        Runtime.getRuntime().exec(
                arrayOf(
                        "psql", "postgres", "-c", "CREATE DATABASE $DB_NAME;")).printErrorAndExitCode()
        DatabaseMigrator(DB_URL, null, null).migrate()
        dbSessionManager = DBSessionManager(DataSourceConnectionProvider(HikariDataSource(HikariConfig().also {
            it.jdbcUrl = DB_URL
        })))
        return dbSessionManager
    }

    private fun Process.printErrorAndExitCode() {
        val reader = BufferedReader(InputStreamReader(errorStream))
        for (line in reader.lines()) {
            println(line)
        }
        val exitCode = waitFor()
        println(exitCode)
        if (exitCode != 0) {
            System.exit(exitCode)
        }
    }
}