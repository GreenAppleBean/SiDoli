package kr.ac.tukorea.greenapple.sidoli

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_maps.*
import kr.ac.tukorea.greenapple.sidoli.api_directions.getDirectionsAPI
import kr.ac.tukorea.greenapple.sidoli.api_lamp.LampItemData
import kr.ac.tukorea.greenapple.sidoli.api_lamp.getLampAPI
import kr.ac.tukorea.greenapple.sidoli.api_police.getPoliceAPI
import kr.ac.tukorea.greenapple.sidoli.databinding.ActivityMapsBinding
import kr.ac.tukorea.greenapple.sidoli.sql.AssetDatabaseOpenHelper

/**
 * 자잘한 버그들..
 * 1. 보안등 버튼이 한 번 클릭 되면 재클릭 시 지워져도 확대, 축소 시 다시 뜸 (setOnCameraListener 때문인 것 같은데 자세한 건 알아봐야 함)
 * 2. 안심벨 울리기는 on/off 방식으로 구현해놓긴 했는데, 실제로 울리는 지는 실제 디바이스에서 테스트해봐야할 듯!!
 * 3. 경찰서 목록이 뜨긴 하는데, 두 번째로 클릭해야만 그에 맞는 적합한 경로가 뜸 (뭔가 클릭이 한 번씩 밀리는 것 같음)
 */

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var adb:AssetDatabaseOpenHelper
    lateinit var pp:SQLiteDatabase
    private lateinit var clusterManager: ClusterManager<LampItemData>

    private lateinit var fusedLocationClient: FusedLocationProviderClient // 위치값 사용
    private lateinit var locationCallback: LocationCallback // 위치값 요청에 대한 갱신 정보를 받아옴

    lateinit var initial_marker : Marker    // 현재 위치 마커(갱신용)
    lateinit var initial_location : Location    // 현재 위치를 Location 객체로 저장해둠(갱신용)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 지도 권한을 array에 저장
        val permission = arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)
        requirePermissions(permission, 999) // 권한 요청을 함

        // 공공데이터로부터 바로 읽어오기()
        adb = AssetDatabaseOpenHelper(this) // assets에 있는 db파일(램프 데이터) 불러오기
        pp = adb.openDatabase() // database 열기

        // 쿼리문을 통해 데이터가 있는 지 검사하기
        insertPoliceData()  // 경찰서 위치에 대한 데이터를 db에 추가함
        //insertLampData()  // 보안등 관련 api 문제로 구현은 안 해놓음 (필요시 추후 업데이트 예정)

        // 0. 현제 위치 마커로 찍고 카메라 위치 옮기기
        var GPS_flag = 0    // 버튼의 on/off 구현을 위한 flag 설정
        GPS.setOnClickListener{
            if(GPS_flag == 0) {
                setLastLocation(initial_location)   // 현 위치로 카메라 설정 및 마커 찍기
                GPS_flag = 1
            }
            else if(GPS_flag == 1){
                initial_marker.remove() // 현 위치 마커 지우기
                GPS_flag = 0
            }
        }

        // 1. 램프 켜기 버튼(클러스터가 확대 축소 시에 무조건 다시 뜨는 버그 있음)
        var Light_flag = 0  // 버튼의 on/off 구현을 위한 flag 설정
        LightBtn.setOnClickListener{
            if(Light_flag == 0) {
                addClusterOnMap()   // map에 가로등 클러스터 구현
                Light_flag = 1
            }
            else if(Light_flag == 1){
                mMap.clear()    // 지도에 있는 모든 표시들 지움(클러스터 일시적으로 지워지긴 하나 확대 및 축소 시 다시 살아남)
                Light_flag = 0
            }
        }

        // 2. 안심벨 울리기 버튼
        val uriRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) // 안심벨용 기본 알림음(별도 파일x, 내장용)
        val ringtone = RingtoneManager.getRingtone(this, uriRingtone)
        var Bell_flag = 0   // 버튼의 on/off 구현을 위한 flag 설정
        BellBtn.setOnClickListener{
            if(Bell_flag == 0){
                ringtone.play() // 소리 울림
                Bell_flag = 1
            }
            else if(Bell_flag == 1){
                ringtone.stop() // 소리 끔
                Bell_flag = 0
            }
        }

        // 3. 경찰서 전화걸기
        CallBtn.setOnClickListener {
            var intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:112")  // intent를 이용해 경찰서로 전화거는 기능 구현
            if(intent.resolveActivity(packageManager) != null){
                startActivity(intent)
            }
        }

        // 4. 경찰서 목록이 뜨고 그 중에서 하나 선택하면 현위치로부터 경로 찍어주기
        PoliceBtn.setOnClickListener {
            var popupMenu = PopupMenu(applicationContext, PoliceBtn)    // 팝업 메뉴를 통한 구현
            menuInflater?.inflate(R.menu.menu_police, popupMenu.menu)

            var listener = PopupMenuListener()  // 리스너는 아래에 내부 클래스로 구현해둠
            popupMenu.setOnMenuItemClickListener(listener)
            popupMenu.show()
        }

    }

    // 팝업 메뉴를 위한 내부 클래스
    inner class PopupMenuListener:PopupMenu.OnMenuItemClickListener{
        val policeArray = adb.PoliceDataExtract(pp) // 경찰서 위치 데이터 뽑아내기
        override fun onMenuItemClick(p0: MenuItem?): Boolean {
            when(p0?.itemId){   // 각각 경찰서 위치에 맞는 경로를 설정
                R.id.world -> {
                    mMap.clear()
                    getcurrentDirection("${initial_location.latitude},${initial_location.longitude}",
                        "${policeArray[0].latitude},${policeArray[0].longitude}")
                }
                R.id.world2 -> {
                    mMap.clear()
                    getcurrentDirection("${initial_location.latitude},${initial_location.longitude}",
                        "${policeArray[1].latitude},${policeArray[1].longitude}")
                }
                R.id.world3 -> {
                    mMap.clear()
                    getcurrentDirection("${initial_location.latitude},${initial_location.longitude}",
                        "${policeArray[2].latitude},${policeArray[2].longitude}")
                }
                R.id.world4 -> {
                    mMap.clear()
                    getcurrentDirection("${initial_location.latitude},${initial_location.longitude}",
                        "${policeArray[3].latitude},${policeArray[3].longitude}")
                }
                R.id.world5 -> {
                    mMap.clear()
                    getcurrentDirection("${initial_location.latitude},${initial_location.longitude}",
                        "${policeArray[4].latitude},${policeArray[4].longitude}")
                }
                R.id.world6 -> {
                    mMap.clear()
                    getcurrentDirection("${initial_location.latitude},${initial_location.longitude}",
                        "${policeArray[5].latitude},${policeArray[5].longitude}")
                }
                R.id.world7 -> {
                    mMap.clear()
                    getcurrentDirection("${initial_location.latitude},${initial_location.longitude}",
                        "${policeArray[6].latitude},${policeArray[6].longitude}")
                }
                R.id.world8 -> {
                    mMap.clear()
                    getcurrentDirection("${initial_location.latitude},${initial_location.longitude}",
                        "${policeArray[7].latitude},${policeArray[7].longitude}")
                }
                R.id.world9 -> {
                    mMap.clear()
                    getcurrentDirection("${initial_location.latitude},${initial_location.longitude}",
                        "${policeArray[8].latitude},${policeArray[8].longitude}")
                }
                R.id.world10 -> {
                    mMap.clear()
                    getcurrentDirection("${initial_location.latitude},${initial_location.longitude}",
                        "${policeArray[9].latitude},${policeArray[9].longitude}")
                }
            }
            return false
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        pp = adb.openDatabase()

        clusterManager = ClusterManager(this, mMap)
        mMap.setOnCameraIdleListener(clusterManager)    // 줌 인 줌 아웃을 trigger 삼아 작동하는 듯
        mMap.setOnMarkerClickListener(clusterManager)   // 마커 클릭 listener

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // 현재 위치 설정을 위한 코드

        // 일단 한국공학대학교 위치를 기준으로 기본 카메라 줌 위치를 설정했습니다.
        // 한국공학대학교(37.3401906, 126.7335293)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.3401906,126.7335293), 15f))

        updateLocation()    // 현재 위치를 설정해줌
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    fun startProcess(){ // 권한이 생길 시 map을 띄워주는 메소드
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // 지도 위치 권한 관련 메소드1
    fun requirePermissions(permissions: Array<String>, requestCode: Int) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permissionGranted(requestCode)
        } else {
            val isAllPermissionsGranted = permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
            if (isAllPermissionsGranted) {
                permissionGranted(requestCode)
            } else {
                ActivityCompat.requestPermissions(this, permissions, requestCode)
            }
        }
    }

    // 지도 위치 권한 관련 메소드2
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            permissionGranted(requestCode)
        } else {
            permissionDenied(requestCode)
        }
    }

    // 권한이 있는 경우 실행
    fun permissionGranted(requestCode: Int) {
        startProcess() // 권한이 있는 경우 구글 지도를 준비하는 코드 실행
    }

    // 권한이 없는 경우 실행
    fun permissionDenied(requestCode: Int) {
        Toast.makeText(this
            , "권한 승인이 필요합니다."
            , Toast.LENGTH_LONG)
            .show()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // 위치 정보를 받아오는 역할
    @SuppressLint("MissingPermission") //requestLocationUpdates는 권한 처리가 필요한데 현재 코드에서는 확인 할 수 없음. 따라서 해당 코드를 체크하지 않아도 됨.
    fun updateLocation() {  // onMapReady가 실행되고 나서 실행되어야 작동함!!
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0?.let {
                    for(location in it.locations) {
                        initial_location = location // 현위치 변수 초기화
                        Log.d("Location", "${location.latitude} , ${location.longitude}")   // 확인용
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
    }

    // 현위치에 마커 추가하고 카메라의 위치를 옮기는 메소드
    fun setLastLocation(lastLocation: Location){
        val LATLNG = LatLng(lastLocation.latitude, lastLocation.longitude)
        val markerOptions = MarkerOptions()
            .position(LATLNG)
            .title("Here!")

        val cameraPosition = CameraPosition.Builder()
            .target(LATLNG)
            .zoom(15.0f)
            .build()

        initial_marker = mMap.addMarker(markerOptions)  // 마커 추가하고
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))  // 카메라 위치 옮김
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // 경로 설정용 메소드
    private fun getcurrentDirection(startLoc:String, endLoc:String) {   // 출발점 좌표와 도착점 좌표 읽어옴
        getDirectionsAPI(pp).getDirectionsData(startLoc,endLoc)
        val cursor = pp.rawQuery("SELECT * From Directions;", null)
        while (cursor.moveToNext()) {
            val point1 = LatLng(cursor.getDouble(0), cursor.getDouble(1))
            val point2 = LatLng(cursor.getDouble(2), cursor.getDouble(3))
            mMap.addPolyline(PolylineOptions()
                .clickable(true)
                .add(point1, point2)
            )
        }
        cursor.close()
    }

    // 공공데이터로부터 램프 데이터 읽어오기
    private fun insertLampData() {
        val lampExistTest = pp.rawQuery("SELECT * FROM LampData;", null)
        if (!lampExistTest.moveToNext()) {   // db 파일에 가로등 data가 없을 시
            getLampAPI(pp).getLampData()    // api에서 가로등 data 끌어와서 집어넣음
        }
        lampExistTest.close()   // 데이터베이스 닫기
    }

    // 공공데이터로부터 경찰서 위치 데이터 읽어오기
    private fun insertPoliceData() {
        val policeExistTest = pp.rawQuery("SELECT * FROM PoliceData;", null)
        if (!policeExistTest.moveToNext()) {   // db 파일에 경찰 data가 없을 시
            getPoliceAPI(pp).getPoliceData()    // api에서 경찰 data 끌어와서 집어넣음
        }
        policeExistTest.close()   // 데이터베이스 닫기
    }

    // 경찰서 위치 지도에 마커로 표시해주는 메소드(구현은 해놓았으니 실 사용은 안 하게 됨)
    private fun addMarkerOnMap() {
        val policeArray = adb.PoliceDataExtract(pp)
        // 경찰 data 마커 표시하기 (파랑색)
        for (idx: Int in 0 until policeArray.size) {
            val police = LatLng(policeArray[idx].latitude, policeArray[idx].longitude)
            mMap.addMarker(MarkerOptions().position(police).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        }
    }

    // 안전등 위치 지도에 클러스터로 표시해주는 메소드
    private fun addClusterOnMap(){
        val lampArray = adb.LampDataExtract(pp)
        // for loop을 돌려서 지도에 클러스터링 마커를 찍어줌
        for (idx: Int in 0 until lampArray.size) {
            clusterManager.addItem(LampItemData(lampArray[idx].latitude.toString(),
                lampArray[idx].longitude.toInt(),
                lampArray[idx].latitude.toString(),
                lampArray[idx].longitude.toString()))
        }
    }
}