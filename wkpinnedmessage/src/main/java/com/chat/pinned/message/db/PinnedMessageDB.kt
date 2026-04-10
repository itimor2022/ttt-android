package com.chat.pinned.message.db

import android.content.ContentValues
import android.database.Cursor
import com.chat.base.WKBaseApplication
import com.chat.base.db.WKCursor
import com.chat.pinned.message.entity.PinnedMessage

class PinnedMessageDB private constructor() {
    private val table = "pinned_message"

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = PinnedMessageDB()
    }

    fun getMaxVersion(channelId: String, channelType: Int): Long {
        if (WKBaseApplication.getInstance().dbHelper == null) {
            return 0
        }
        val sql =
            "select * from $table where channel_id=? and channel_type=? order by `version` desc limit 1"
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.rawQuery(
            sql,
            arrayOf(channelId, channelType.toString())
        )
        cursor.moveToFirst()
        var num = 0L
        if (!cursor.isAfterLast) {
            val word = serialize(cursor)
            num = word.version
            cursor.moveToNext()
        }
        cursor.close()
        return num
    }

    fun queryPinnedMessage(channelId: String, channelType: Int): List<PinnedMessage> {
        // val sql = "select * from $table where channel_id=? and channel_type=? ORDER BY message_seq ASC"
        val list = ArrayList<PinnedMessage>()
        val cursor: Cursor = WKBaseApplication.getInstance().dbHelper.select(
            table,
            "channel_id=? and channel_type=? and is_deleted=0",
            arrayOf(channelId, channelType.toString()),
            "message_seq ASC"
        )
            ?: return list
        run {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(serialize(cursor))
                cursor.moveToNext()
            }
        }
        return list
    }

    fun insertPinnedMessage(list: List<PinnedMessage>) {
        val insertCVList = ArrayList<ContentValues>()
        for (pinnedMessage in list) {
            insertCVList.add(getCV(pinnedMessage))
        }
        WKBaseApplication.getInstance().dbHelper.db.beginTransaction()
        try {
            for (cv in insertCVList) {
                WKBaseApplication.getInstance().dbHelper.insertOrReplace(table, cv)
            }
            WKBaseApplication.getInstance().dbHelper.db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            WKBaseApplication.getInstance().dbHelper.db.endTransaction()
        }
    }

    private fun getCV(pinnedMessage: PinnedMessage): ContentValues {
        val cv = ContentValues()
        cv.put("message_id", pinnedMessage.message_id)
        cv.put("message_seq", pinnedMessage.message_seq)
        cv.put("channel_id", pinnedMessage.channel_id)
        cv.put("channel_type", pinnedMessage.channel_type)
        cv.put("is_deleted", pinnedMessage.is_deleted)
        cv.put("version", pinnedMessage.version)
        cv.put("created_at", pinnedMessage.created_at)
        cv.put("updated_at", pinnedMessage.updated_at)
        return cv
    }

    private fun serialize(cursor: Cursor): PinnedMessage {
        val word = PinnedMessage()
        word.message_id = WKCursor.readString(cursor, "message_id")
        word.message_seq = WKCursor.readLong(cursor, "message_seq")
        word.channel_id = WKCursor.readString(cursor, "channel_id")
        word.channel_type = WKCursor.readInt(cursor, "channel_type")
        word.is_deleted = WKCursor.readInt(cursor, "is_deleted")
        word.version = WKCursor.readLong(cursor, "version")
        word.created_at = WKCursor.readString(cursor, "created_at")
        word.updated_at = WKCursor.readString(cursor, "updated_at")
        return word
    }
}