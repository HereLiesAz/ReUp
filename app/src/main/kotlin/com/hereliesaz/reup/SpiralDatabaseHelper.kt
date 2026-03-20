package com.hereliesaz.reup

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyDistortion(val date: String, val count: Int)

class SpiralDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SpiralLog.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "distortions"
        const val COLUMN_ID = "id"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_WORD = "word"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIMESTAMP INTEGER,
                $COLUMN_WORD TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun logDistortion(word: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
            put(COLUMN_WORD, word)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getDailyCounts(): List<DailyDistortion> {
        val db = this.readableDatabase
        val counts = LinkedHashMap<String, Int>()
        val cursor = db.rawQuery("SELECT $COLUMN_TIMESTAMP FROM $TABLE_NAME ORDER BY $COLUMN_TIMESTAMP ASC", null)
        
        val formatter = SimpleDateFormat("MM/dd", Locale.getDefault())
        
        if (cursor.moveToFirst()) {
            do {
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                val dateString = formatter.format(Date(timestamp))
                counts[dateString] = counts.getOrDefault(dateString, 0) + 1
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        
        return counts.map { DailyDistortion(it.key, it.value) }
    }
}
