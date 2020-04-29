package org.mozilla.msrp.platform.push.repository

import org.mozilla.msrp.platform.push.util.PushAdminMetrics
import java.sql.SQLException
import javax.inject.Inject
import javax.inject.Named
import javax.sql.DataSource

@Named
class PushLogRepository @Inject constructor(val dataSource: DataSource) {

    companion object {
        private const val SQL_ADD_WORK = "INSERT INTO worker ( " +
                "task, data, status )\n" +
                "VALUES ( 'push_worker', ?, 'new') RETURNING id"
    }

    fun addWork(data: String): Int? {
        try {
            dataSource.connection.use {
                val prepareStatement = it.prepareStatement(SQL_ADD_WORK)
                prepareStatement.setString(1, data)
                val result = prepareStatement.executeQuery()
                result.next()
                val id = result.getInt("id")
                return id
            }
        } catch (e: SQLException) {
            PushAdminMetrics.event(PushAdminMetrics.LOG_PUBSUB_ERROR_QUERY, e.localizedMessage)
            return null
        }
    }
}
