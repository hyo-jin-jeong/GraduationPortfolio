package com.kakao.smartmemo

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.map_fragment.view.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView


class MapFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.map_fragment, container, false)

        val mapView = MapView(view.context)
        val mapViewContainer = view.map_view as ViewGroup
        mapViewContainer.addView(mapView)

        val mapPoint = MapPoint.mapPointWithGeoCoord(37.581759, 127.010337)
        val customMarker = MapPOIItem()
        customMarker.itemName = "Custom Marker"
        customMarker.tag = 1
        customMarker.mapPoint = mapPoint

        //customMarker가 너무 큼 수정하고 사용하도록 함
        //customMarker.markerType = MapPOIItem.MarkerType.CustomImage // 마커타입을 커스텀 마커로 지정.
        customMarker.markerType = MapPOIItem.MarkerType.BluePin
        customMarker.selectedMarkerType = MapPOIItem.MarkerType.RedPin

        customMarker.customImageResourceId = R.drawable.memo_marker // 마커 이미지.

        customMarker.isCustomImageAutoscale =
            false // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.

        customMarker.setCustomImageAnchor(
            0.5f,
            1.0f
        ) // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.


        mapView.addPOIItem(customMarker)
        mapView.setPOIItemEventListener()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater:MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);

        (activity as MainActivity).toolbar.title = resources.getString(R.string.tab_text_1)
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.select_group_in_map, menu)
        menu.getItem(1)?.isChecked = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //학교를 눌렀을 때
            R.id.action_settings1 -> {
                item.isChecked = !item.isChecked
                when(item.isChecked) {
                    true -> Toast.makeText(view?.context, item.title, Toast.LENGTH_SHORT).show()
                }
                true
            }
            //가족을 눌렀을 때
            R.id.action_settings2 -> {
                item.isChecked = !item.isChecked
                when(item.isChecked) {
                    true -> Toast.makeText(view?.context, item.title, Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

private fun MapView.setPOIItemEventListener() {
    //여기는 고치는 중이라 안지울게요
//    val fm: FragmentManager =
//    val overlay = FragmentDialog()
//    overlay.show(fm, "FragmentDialog")

}