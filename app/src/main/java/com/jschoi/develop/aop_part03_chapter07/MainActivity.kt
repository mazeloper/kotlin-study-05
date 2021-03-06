package com.jschoi.develop.aop_part03_chapter07

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.jschoi.develop.aop_part03_chapter07.adapter.HouseListAdapter
import com.jschoi.develop.aop_part03_chapter07.adapter.HouseViewPagerAdapter
import com.jschoi.develop.aop_part03_chapter07.dto.HouseDto
import com.jschoi.develop.aop_part03_chapter07.model.HouseModel
import com.jschoi.develop.aop_part03_chapter07.net.HouseService
import com.jschoi.develop.aop_part03_chapter07.net.RetrofitClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * Simple Airbnb App
 *
 * https://navermaps.github.io/android-map-sdk/guide-ko/
 */
class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var mRetofit: Retrofit
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val mapView: MapView by lazy {
        findViewById(R.id.mapView)
    }
    private val myViewPager: ViewPager2 by lazy {
        findViewById(R.id.myViewPager)
    }
    private val myRecyclerView: RecyclerView by lazy {
        findViewById(R.id.myRecyclerView)
    }
    private val currentLocationButton: LocationButtonView by lazy {
        findViewById(R.id.currentLocationButton)
    }
    private val bottomSheetTitleTextView: TextView by lazy {
        findViewById(R.id.bottomSheetTitleTextView)
    }
    private val viewPagerAdapter = HouseViewPagerAdapter(itemClicked = {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "[?????? ??? ????????? ???????????????!!] ${it.title} ${it.price} ???????????? :${it.imageUrl}"
            )
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, null))
    })
    private val recyclerAdapter = HouseListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRetofit = RetrofitClient.getInstance()
        // ViewPager
        myViewPager.adapter = viewPagerAdapter
        myViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val selectedHouseModel = viewPagerAdapter.currentList[position]
                val cameraUpdate =
                    CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat, selectedHouseModel.lng))
                        .animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)
            }
        })

        // ViewPager
        myRecyclerView.layoutManager = LinearLayoutManager(this)
        myRecyclerView.adapter = recyclerAdapter
        // ??????????????? ????????? ?????? ??????????????? ?????? ???????????? ?????? ??????
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    /**
     * MapView??? getMapAsync() ????????????
     * OnMapReadyCallback??? ???????????? ???????????? NaverMap ????????? ?????? ??? ????????????.
     */
    override fun onMapReady(map: NaverMap) {
        naverMap = map

        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        // ??????
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.531492, 126.732827))
        naverMap.moveCamera(cameraUpdate)

        // ????????? - permission(????????????)
        val uiSetting = naverMap.uiSettings
        // uiSetting.isLocationButtonEnabled = true
        uiSetting.isLocationButtonEnabled = false
        // Custom Location Button
        currentLocationButton.map = naverMap

        // ?????? ?????????????????? ?????? ?????? ???????????? ??????
        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        getHouseListFromAPI()
    }

    private fun getHouseListFromAPI() {
        mRetofit.create(HouseService::class.java).getHouseList().also {
            it.enqueue(object : Callback<HouseDto> {
                override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
                    if (response.isSuccessful.not() || response.body() == null) return

                    Log.d("TAG", ">>>>> BODY : ${response.body().toString()}")
                    response.body()?.let { data ->
                        viewPagerAdapter.submitList(data.items)
                        recyclerAdapter.submitList(data.items)

                        bottomSheetTitleTextView.text = "${data.items.size}?????? ??????"
                        updateMarker(data.items)
                    }
                }

                override fun onFailure(call: Call<HouseDto>, t: Throwable) {
                    Log.e("TAG", ">>>> ERROR : ${t.message}")
                }
            })
        }
    }

    private fun updateMarker(house: List<HouseModel>) {
        house.forEach {
            val marker = Marker()
            marker.position = LatLng(it.lat, it.lng)
            marker.onClickListener = this
            marker.map = naverMap
            marker.tag = it.id
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.RED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onClick(overly: Overlay): Boolean {
        if (overly is Marker) {
            val selectedModel = viewPagerAdapter.currentList.firstOrNull {
                it.id == overly.tag
            }

            selectedModel?.let {
                val position = viewPagerAdapter.currentList.indexOf(it)
                myViewPager.currentItem = position
            }
        }
        return true
    }
}