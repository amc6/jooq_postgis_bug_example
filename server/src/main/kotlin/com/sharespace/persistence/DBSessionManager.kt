package com.sharespace.persistence

import org.jooq.ConnectionProvider
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.ThreadLocalTransactionProvider
import javax.transaction.TransactionRequiredException

class DBSessionManager(connectionProvider: ConnectionProvider) {

    private val threadLocalContext: DSLContext
    private val inTransaction: ThreadLocal<Boolean> = ThreadLocal.withInitial({ false })

    init {
        val transactionProvider = ThreadLocalTransactionProvider(connectionProvider, true)
        val threadLocalConfig = DefaultConfiguration().set(SQLDialect.POSTGRES).set(transactionProvider)
        threadLocalContext = DSL.using(threadLocalConfig)
    }

    // Top-level transactions must begin registered here.
    fun <T> startTransaction(body: (DSLContext) -> T): T {
        if (inTransaction.get()) {
            throw RuntimeException("Already in a top-level transaction.")
        }

        inTransaction.set(true)

        try {
            return transaction(body)
        } finally {
            inTransaction.set(false)
        }
    }

    @Suppress("RedundantLambdaArrow") //incorrect type inference by intellij
    // Nested transactions should happen here.
    fun <T> transaction(body: (DSLContext) -> T): T {
        if (!inTransaction.get()) throw TransactionRequiredException()

        return threadLocalContext.transactionResult { ->
            body(threadLocalContext)
        }
    }

    fun session(): DSLContext {
        if (!inTransaction.get()) throw TransactionRequiredException()
        return threadLocalContext
    }
}