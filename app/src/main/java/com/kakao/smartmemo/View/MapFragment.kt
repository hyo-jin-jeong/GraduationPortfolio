package com.kakao.smartmemo.View

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kakao.smartmemo.Adapter.LocationAdapter
import com.kakao.smartmemo.ApiConnect.ApiClient
import com.kakao.smartmemo.ApiConnect.ApiInterface
import com.kakao.smartmemo.ApiConnect.CategoryResult
import com.kakao.smartmemo.ApiConnect.Document
import com.kakao.smartmemo.Contract.MapContract
import com.kakao.smartmemo.Data.PlaceData
import com.kakao.smartmemo.Presenter.MapPresenter
import com.kakao.smartmemo.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_dialog.toolbar
import kotlinx.android.synthetic.main.map_fragment.view.*
import net.daum.mf.map.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapFragment : Fragment(), MapView.POIItemEventListener, MapView.MapViewEventListener,
    MapContract.View,
    MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener,
    MapView.OpenAPIKeyAuthenticationResultListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private lateinit var presenter: MapPresenter
    private lateinit var goCurLocation: FloatingActionButton

    private lateinit var mapView: MapView
    private lateinit var mapViewContainer: ViewGroup
    private var usingMapView = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter

    private var documentList: ArrayList<Document> = ArrayList()
    private var todo = mutableListOf<PlaceData>()
    private var memo = mutableListOf<PlaceData>()

    private var isLongTouch: Boolean = false

    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mServiceHandler: Handler? = null
    private var curLocationMarker: MapPOIItem = MapPOIItem()
    private var convertedAddress: String? = null
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        presenter = MapPresenter(this)
        return inflater.inflate(R.layout.map_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = MapView(view.context)
        usingMapView = true

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        locationSetting()

        mapViewContainer = view.map_view as ViewGroup
        mapViewContainer.addView(mapView)

        mapView.setPOIItemEventListener(this)
        mapView.setMapViewEventListener(this)
        mapView.setCurrentLocationEventListener(this)
        mapView.setOpenAPIKeyAuthenticationResultListener(this)

        goCurLocation = view.findViewById(R.id.go_curLocation)
        goCurLocation.setOnClickListener {
            if(mapView.currentLocationTrackingMode == MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving) {
                goCurLocation.setImageResource(R.drawable.current_location_click_icon)
                mapView.currentLocationTrackingMode =
                    MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
            } else {
                goCurLocation.setImageResource(R.drawable.current_location_icon)
                mapView.currentLocationTrackingMode =
                    MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
            }
        }
        recyclerView = view.findViewById(R.id.map_recyclerview)
        val layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) //레이아웃매니저 생성

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        ) //아래구분선 세팅

        recyclerView.layoutManager = layoutManager

    }

    private fun locationSetting() {
        createLocationRequest()
        getLastLocation()
        val handlerThread = HandlerThread(MapFragment::class.java.simpleName)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10000
        mLocationRequest!!.fastestInterval = 5000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient!!.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        currentLocation = task.result
                        mapView.setMapCenterPoint(
                            MapPoint.mapPointWithGeoCoord(currentLocation!!.latitude, currentLocation!!.longitude!!),
                            false
                        )
                    } else {
                        Log.w(
                            "check",
                            "Failed to get location."
                        )
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(
                "check",
                "Lost location permission.$unlikely"
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("jieun", "usingMapView = $usingMapView")
        if (!usingMapView) {
            mapView = MapView(view!!.context)
            mapViewContainer.addView(mapView)
            mapView.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
            mapView.setPOIItemEventListener(this)
            mapView.setMapViewEventListener(this)
            mapView.setCurrentLocationEventListener(this)
            mapView.setOpenAPIKeyAuthenticationResultListener(this)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.getTodoPlaceAlarm()
        presenter.getMemo()

    }
    override fun onPause() {
        super.onPause()
        usingMapView = false
        Log.e("jieun", "이것을 스침.")
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        (activity as MainActivity).toolbar.title = resources.getString(R.string.tab_text_1)
        (activity as MainActivity).fab.visibility = View.VISIBLE
        (activity as MainActivity).fab_todo.visibility = View.VISIBLE
        (activity as MainActivity).fab_memo.visibility = View.VISIBLE
        val plusButton = (activity as MainActivity).fab

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.search_view_in_map, menu)

        val searchItem: MenuItem? = menu.findItem(R.id.search)
        val searchView = searchItem!!.actionView as SearchView

        var firstX: String? = null
        var firstY: String? = null

        locationAdapter = LocationAdapter(documentList, context!!, searchView, recyclerView)
        recyclerView.adapter = locationAdapter

        searchView.setOnCloseListener {
            goCurLocation.visibility = FloatingActionButton.VISIBLE
            plusButton.visibility = Button.VISIBLE
            false
        }
        searchView.setOnSearchClickListener {
            goCurLocation.visibility = FloatingActionButton.INVISIBLE
            plusButton.visibility = Button.INVISIBLE
            false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { // do your logic here
                locationAdapter.notifyDataSetChanged()
                plusButton.visibility = Button.VISIBLE
                goCurLocation.visibility = FloatingActionButton.VISIBLE
                recyclerView.visibility = View.GONE
                Toast.makeText(context, query, Toast.LENGTH_SHORT).show()
                if(locationAdapter.clicked) {
                    changeMapCenterPoint(locationAdapter.selectedX, locationAdapter.selectedY)
                    val point: MapPoint = MapPoint.mapPointWithGeoCoord(locationAdapter.selectedY!!.toDouble(), locationAdapter.selectedX!!.toDouble())
                    curLocationMarker = createMarker("current marker", point, R.drawable.cur_location_icon)
                    mapView.addPOIItem(curLocationMarker)
                    presenter.convertAddressFromMapPOIItem(curLocationMarker)
                } else {
                    //첫번째 요소의 포인트로 이동
                    changeMapCenterPoint(firstX, firstY)
                    val point: MapPoint = MapPoint.mapPointWithGeoCoord(firstY!!.toDouble(), firstX!!.toDouble())
                    curLocationMarker = createMarker("current marker", point, R.drawable.cur_location_icon)
                    mapView.addPOIItem(curLocationMarker)
                    presenter.convertAddressFromMapPOIItem(curLocationMarker)
                }
                searchView.clearFocus()
                locationAdapter.clicked = false
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()) {
                    documentList.clear()
                    locationAdapter.clear()
                    locationAdapter.notifyDataSetChanged()
                    val apiClient: ApiClient =
                        ApiClient()
                    val apiInterface: ApiInterface = apiClient.getApiClient()!!.create(
                        ApiInterface::class.java
                    )
                    val callKeyword: Call<CategoryResult?>? = apiInterface.getSearchLocation(
                        getString(R.string.kakao_restapi_key),
                        "$newText ",
                        15
                    )
                    val callbackKeyword: Callback<CategoryResult?> = object : Callback<CategoryResult?> {
                        //리스폰 시, 대응할 구현체
                        override fun onResponse(
                            call: Call<CategoryResult?>,
                            response: Response<CategoryResult?>
                        ) {
                            if (response.isSuccessful) { //check for Response status
                                assert(response.body() != null)
                                for (document in response.body()?.getDocuments()!!) {
                                    firstX = response.body()?.getDocuments()!![0]!!.x
                                    firstY = response.body()?.getDocuments()!![0]!!.y
                                    locationAdapter.addItem(document!!)
                                }
                                locationAdapter.notifyDataSetChanged()
                            } else {
                                val statusCode = response.code()
                                val responseBody = response.body()
                            }
                        }

                        override fun onFailure(
                            call: Call<CategoryResult?>,
                            t: Throwable
                        ) {
                        }
                    }
                    callKeyword!!.enqueue(callbackKeyword)

                    recyclerView.visibility = View.VISIBLE
                } else {
                    goCurLocation.visibility = FloatingActionButton.INVISIBLE
                    recyclerView.visibility = View.GONE
                    plusButton.visibility = Button.INVISIBLE
                }
                return false
            }
        })

        val searchManager = this.context!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo((activity as MainActivity).componentName))

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
    }

    //setPOIItemEventListener override method
    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {

    }

    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {

    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {

    }

    //marker 선택 시
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        val dialog = DialogFragment()
        //type은 memo만이면 0, todo만이면 1, 둘다면 2
        when (p1?.customImageResourceId) {
            R.drawable.memo_icon -> {
                dialog.setCurType(0)
                dialog.show(super.getChildFragmentManager(), "show dialog")
            }
            R.drawable.todo_icon -> {
                dialog.setCurType(1)
                dialog.show(super.getChildFragmentManager(), "show dialog")
            }
            R.drawable.memo_todo_icon -> {
                dialog.setCurType(2)
                dialog.show(super.getChildFragmentManager(), "show dialog")
            }
            else -> {
                val items = arrayOf<CharSequence>("메모", "TODO 장소알람")
                val listDialog: AlertDialog.Builder = AlertDialog.Builder(
                    this.context,
                    android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
                )
                listDialog.setTitle("이 곳에 추가할 것을 선택하세요")
                    .setItems(items, DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            0 -> {
                                val addMemoIntent = Intent(this.context, AddMemo::class.java)
                                val point = curLocationMarker.mapPoint.mapPointGeoCoord
                                val placeData = PlaceData(convertedAddress!!, point.latitude, point.longitude)
                                addMemoIntent.putExtra("placeData", placeData)
                                Log.e("jieun", "long press한 위치의 주소는 $convertedAddress")
                                startActivity(addMemoIntent)
                                this.onDestroyView()
                                mapView.removePOIItem(curLocationMarker)
                            }
                            1 -> {
                                val addTodoIntent = Intent(this.context, PlaceAlarmDetailActivity::class.java)
                                val point = curLocationMarker.mapPoint.mapPointGeoCoord
                                val placeData = PlaceData(convertedAddress!!, point.latitude, point.longitude)
                                addTodoIntent.putExtra("placeData", placeData)
                                addTodoIntent.putExtra("mode", "longPressed")
                                Log.e("jieun", "long press한 위치의 주소는 $convertedAddress")
                                startActivity(addTodoIntent)
                                this.onDestroyView()
                                usingMapView = false
                                mapView.onPause()
                                mapViewContainer.removeAllViews()
                                mapView.removePOIItem(curLocationMarker)
                            }
                        }
                    })
                    .show()
            }
        }
    }


    //setMapViewEventListener override method
    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewInitialized(p0: MapView?) {
        val mapPoint1 = MapPoint.mapPointWithGeoCoord(37.565841, 126.976825)
        val memoAndTodo = createMarker("Memo And Todo", mapPoint1, R.drawable.memo_todo_icon)
        mapView.addPOIItem(memoAndTodo)

        val mapPoint2 = MapPoint.mapPointWithGeoCoord(37.565799, 126.975183)
        val memo = createMarker("Memo", mapPoint2, R.drawable.memo_icon)
        mapView.addPOIItem(memo)

        val mapPoint3 = MapPoint.mapPointWithGeoCoord(37.564170, 126.978471)
        val todo = createMarker("Todo", mapPoint3, R.drawable.todo_icon)
        mapView.addPOIItem(todo)
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
        mapView.removePOIItem(curLocationMarker)
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
        recyclerView.visibility = View.GONE
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
        val handler = Handler()
        val then: Long = 0
        Log.i("jieun", "LongPress 시작")

        //이거는 확인하고 시간있으면 고쳐야해서 둠
        p0!!.setOnTouchListener(object : View.OnTouchListener {
            private val longClickDuration = 1500L

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_UP) {
                    if ((System.currentTimeMillis() - then) > longClickDuration) {
                        Log.i("jieun", "클릭을 뗌!")
                        startLongPress(p1!!)
                    }
                }
                return true
            }
        })

        handler.postDelayed({
            p0!!.setOnTouchListener(null)
            Log.i("jieun", "삭제 ㅠ")
        }, 2000L)

    }


    fun startLongPress(p1: MapPoint) {
        val curPoint = p1.mapPointGeoCoord
        val longitude = curPoint.longitude
        val latitude = curPoint.latitude

        //주소 정보도 포함하기
        when {
            !isLongTouch -> isLongTouch = true
            else -> mapView.removePOIItem(curLocationMarker)
        }
        curLocationMarker = createMarker("current marker", p1!!, R.drawable.cur_location_icon)
        mapView.addPOIItem(curLocationMarker)
        presenter.convertAddressFromMapPOIItem(curLocationMarker)

        val items = arrayOf<CharSequence>("메모", "TODO 장소알람")
        val listDialog: AlertDialog.Builder = AlertDialog.Builder(
            this.context,
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
        )
        listDialog.setTitle("이 곳에 추가할 것을 선택하세요")
            .setItems(items, DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 -> {
                        val addMemoIntent = Intent(this.context, AddMemo::class.java)
                        val placeData = PlaceData(convertedAddress!!, latitude, longitude)
                        addMemoIntent.putExtra("placeData", placeData)
                        Log.e("jieun", "long press한 위치의 주소는 $convertedAddress")
                        startActivity(addMemoIntent)
                        this.onDestroyView()
                        mapView.removePOIItem(curLocationMarker)
                    }
                    1 -> {
                        val addTodoIntent = Intent(this.context, PlaceAlarmDetailActivity::class.java)
                        val placeData = PlaceData(convertedAddress!!, latitude, longitude)
                        addTodoIntent.putExtra("placeData", placeData)
                        addTodoIntent.putExtra("mode", "longPressed")
                        Log.e("jieun", "long press한 위치의 주소는 $convertedAddress")
                        startActivity(addTodoIntent)
                        this.onDestroyView()
                        usingMapView = false
                        mapView.onPause()
                        mapViewContainer.removeAllViews()
                        mapView.removePOIItem(curLocationMarker)
                    }
                }
            })
            .show()
    }


    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
    }

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {

    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {

    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {

    }

    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {

    }

    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, p1: String?) {
        p0.toString()
    }

    private fun createMarker(name: String, point: MapPoint, imageResourceId: Int): MapPOIItem {
        val marker = MapPOIItem()
        marker.itemName = name
        marker.mapPoint = point
        marker.markerType = MapPOIItem.MarkerType.CustomImage
        marker.customImageResourceId = imageResourceId
        marker.isCustomImageAutoscale = false
        marker.isShowCalloutBalloonOnTouch = false
        marker.setCustomImageAnchor(0.5f, 1.0f)

        return marker
    }

    private fun checkLocationServicesStatus(): Boolean {
        val locationManager: LocationManager? = this@MapFragment.requireActivity().getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager?
        return (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    override fun onDaumMapOpenAPIKeyAuthenticationResult(p0: MapView?, p1: Int, p2: String?) {

    }

    fun changeMapCenterPoint(x: String?, y: String?) {
        if(x != null && y != null) {
            mapView.moveCamera(CameraUpdateFactory.newMapPoint(MapPoint.mapPointWithGeoCoord(y.toDouble(), x.toDouble())))
        }
    }

    override fun getLocationName(mapPOIItem: MapPOIItem, locationName: String?) {
        convertedAddress = locationName
    }

    override fun onSuccess(placeList: MutableList<PlaceData>, status: String) {
        if (status == "todo") {
            todo = placeList
        } else {
            memo = placeList
        }
    }

}




