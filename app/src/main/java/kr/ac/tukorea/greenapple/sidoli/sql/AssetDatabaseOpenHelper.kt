package kr.ac.tukorea.greenapple.sidoli.sql

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AssetDatabaseOpenHelper(private val context: Context) {
    companion object {
        private val DB_NAME = "APIData.db"
    }

    fun openDatabase(): SQLiteDatabase {
        val dbFile = context.getDatabasePath(DB_NAME)

        if (!dbFile.exists()) {
            try {
                val checkDB = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE,null)

                checkDB?.close()
                copyDatabase(dbFile)
            } catch (e: IOException) {
                throw RuntimeException("Error creating source database", e)
            }
        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
    }

    @SuppressLint("WrongConstant")
    private fun copyDatabase(dbFile: File) {
        val `is` = context.assets.open(DB_NAME)
        val os = FileOutputStream(dbFile)

        val buffer = ByteArray(1024)
        while (`is`.read(buffer) > 0) {
            os.write(buffer)
            Log.d("#DB", "writing>>")
        }

        os.flush()
        os.close()
        `is`.close()
        Log.d("#DB", "completed..")
    }

    // db에서 lamp data만 끌어내는 method
    fun LampDataExtract(database: SQLiteDatabase): ArrayList<lampSortData> {
        var cursor: Cursor
        cursor = database.rawQuery("SELECT * from LAMPData;", null)

        var lampArray = ArrayList<lampSortData>()   // data class의 인스턴스들로 구성된 arraylist를 선언하고 반환함

        while (cursor.moveToNext()) {
            // 행에서 3, 4번째 열에 해당하는 내용 (위도, 경도)을 arraylist에 삽입함
            lampArray.add(lampSortData(cursor.getDouble(2), cursor.getDouble(3)))
        }

        return lampArray
    }

    // db에서 police data만 끌어내는 method
    fun PoliceDataExtract(database: SQLiteDatabase): ArrayList<policeSortData> {
        var cursor: Cursor
        cursor = database.rawQuery("SELECT * from PoliceData;", null)

        var policeArray = ArrayList<policeSortData>()   // data class의 인스턴스들로 구성된 arraylist를 선언하고 반환함

        while (cursor.moveToNext()) {
            // 행에서 3, 4번째 열에 해당하는 내용 (위도, 경도)을 arraylist에 삽입함
            policeArray.add(policeSortData(cursor.getDouble(2), cursor.getDouble(3)))
        }

        return policeArray
    }
}

data class lampSortData(
    val latitude:Double,
    val longitude:Double
)

data class policeSortData(
    val latitude:Double,
    val longitude:Double
)