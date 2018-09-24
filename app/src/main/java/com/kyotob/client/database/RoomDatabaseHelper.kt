package com.kyotob.client.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

data class RoomsUnreadModel(val roomId: Int, val midokuNum: Int)

class RoomDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "ROOMS.db"
        const val TABLE_NAME = "midoku"
        const val COLUMN1 = "room_id"
        const val COLUMN2 = "midoku_num"

        // DBがないとき、バージョンアップ時に、テーブルを作るSQL
        private const val SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN1 + " INTEGER," +
                        COLUMN2 + " INTEGER)"

        // DBのバージョンアップ時にテーブルを削除するSQL
        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    // データ追加用のSQL
    @Throws(SQLiteException::class)
    fun insertData(task: RoomsUnreadModel): Boolean {
        // DBの実体
        val db = writableDatabase

        // 引数に取っているデータを追加するSQL
        val values = ContentValues()
        values.put(COLUMN1, task.roomId)
        values.put(COLUMN2, task.midokuNum)
        // データ追加
        val rowId = db.insert(TABLE_NAME, null, values)
        // 終了
        db.close()
        return true
    }

    // データ更新用のSQL
    @Throws(SQLiteException::class)
    fun updateData(roomId: Int, num: Int): Boolean {
        // DBの実体
        val db = writableDatabase

        // 更新
        val value = ContentValues()
        value.put(COLUMN2, num) // 値をセット
        val rowId = db.update(TABLE_NAME, value, "$COLUMN1 = $roomId", arrayOf()) // SQL文を実行

        // 終了
        db.close()

        return when (rowId) {
            1 -> true
            else -> false
        }
    }

    // データ検索用のSQL(未読件数を返す)
    @Throws(SQLiteException::class)
    fun searchData(roomId: Int): Int {
        // DBの実体
        val db = readableDatabase

        // 検索
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN1 = $roomId", arrayOf()) // SQL文を実行
        val num: Int
        num = if (cursor.moveToFirst()) { // 先頭のフィールドに移動
            cursor.getInt(1) // 値を取得
        } else {
            -1
        }

        // 終了
        cursor.close()
        db.close()
        return num
    }

    // DBがないときに呼び出される
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    // バージョンアップするときに呼び出される
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_ENTRIES)
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    // ダウングレード時に呼び出される
    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}
