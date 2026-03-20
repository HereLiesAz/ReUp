// hereliesaz/reup/ReUp-c714b8692ef249c9d91ed57a33a63f43f5c8c59d/app/src/main/kotlin/com/hereliesaz/reup/SpiralDatabaseHelper.kt

package com.hereliesaz.reup

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Date

// Data structure representing a logged cognitive collapse
data class DistortionEntry(
    val id: Long,
    val text: String,
    val timestamp: Long,
    val focusFlag: Int, // The target of the hostility
    val typeFlag: Int   // The flavor of the toxicity
)

data class DailyDistortion(
    val dateLabel: String,
    val count: Int
)

class SpiralDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "spiral_ledger.db"
        private const val DATABASE_VERSION = 2 // Incremented BUREAUCRACY version

        const val TABLE_LEDGER = "ledger"
        const val COL_ID = "id"
        const val COL_TEXT = "distortion_text"
        const val COL_TIMESTAMP = "timestamp"
        const val COL_FOCUS_FLAG = "focus_flag" // NEW column
        const val COL_TYPE_FLAG = "type_flag"   // NEW column
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            """
            CREATE TABLE $TABLE_LEDGER (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TEXT TEXT NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL,
                $COL_FOCUS_FLAG INTEGER NOT NULL,
                $COL_TYPE_FLAG INTEGER NOT NULL
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Migrating the data structures of your past misery.
            db?.execSQL("ALTER TABLE $TABLE_LEDGER ADD COLUMN $COL_FOCUS_FLAG INTEGER DEFAULT 0")
            db?.execSQL("ALTER TABLE $TABLE_LEDGER ADD COLUMN $COL_TYPE_FLAG INTEGER DEFAULT 0")
        }
    }

    fun logDistortion(text: String, focusFlag: Int, typeFlag: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TEXT, text)
            put(COL_TIMESTAMP, Date().time)
            put(COL_FOCUS_FLAG, focusFlag)
            put(COL_TYPE_FLAG, typeFlag)
        }
        db.insert(TABLE_LEDGER, null, values)
        // db.close() excised. The machine must not suffocate its own threads.
    }

    /**
     * Aggregates the history of your cognitive collapses by day,
     * now tethered to local temporal reality.
     */
    fun getDailyCounts(): List<DailyDistortion> {
        val list = mutableListOf<DailyDistortion>()
        val db = readableDatabase
        
        // Appended 'localtime' to prevent UTC temporal drift.
        val query = "SELECT date(timestamp / 1000, 'unixepoch', 'localtime') as day, count(*) as cnt FROM $TABLE_LEDGER GROUP BY day ORDER BY day ASC LIMIT 7"
        val cursor = db.rawQuery(query, null)
        
        if (cursor.moveToFirst()) {
            do {
                val day = cursor.getString(0) ?: "Unknown"
                val count = cursor.getInt(1)
                list.add(DailyDistortion(day, count))
            } while (cursor.moveToNext())
        }
        cursor.close()
        // db.close() excised. Let the lifecycle handle the burial.
        return list
    }
}
