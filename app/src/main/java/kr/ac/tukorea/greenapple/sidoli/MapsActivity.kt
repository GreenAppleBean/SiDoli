package kr.ac.tukorea.greenapple.sidoli

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kr.ac.tukorea.greenapple.sidoli.databinding.ActivityMapsBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    //lateinit var myHelper: myDBHelper
    //lateinit var sqlDB: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*
        myHelper = myDBHelper(this)
        sqlDB = myHelper.writableDatabase
        myHelper.onUpgrade(sqlDB, 1, 2) // 인수는 아무거나 입력하면 됨.
        sqlDB.close()
         */

        val adb = AssetDatabaseOpenHelper(this)
        adb.openDatabase()
    }

    //
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        /*
        // Add a marker in Sydney and move the camera
        // sample에 대한 위치 설정
        val sample = LatLng(37.45568239,126.8168629)

        // 마커 생성
        mMap.addMarker(MarkerOptions().position(sample).title("Marker sample is here"))

        // camera 위치 옮기기
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sample))
         */

        /*
        for(idx : Int in 0..10){
            val sample = LatLng(37.45568239 + idx,126.8168629)
            mMap.addMarker(MarkerOptions().position(sample))
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(37.45568239,126.8168629)))
         */


    }
}

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
}

/*
// database 생성 및 초기화
class myDBHelper(context: Context) : SQLiteOpenHelper(context, "gongDB", null, 1) {

    override fun onCreate(p0: SQLiteDatabase?) {
        p0!!.execSQL("CREATE TABLE  groupTBL ( gName CHAR(20) PRIMARY KEY, gNumber INTEGER);")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0!!.execSQL("DROP TABLE IF EXISTS groupTBL")
        onCreate(p0)
    }
}
*/