package com.chat.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.chat.map.databinding.ActivityChooseLocationBinding
import java.util.Locale

/**
 * 位置选择页面 - 使用高德地图SDK
 */
class ChooseLocationActivity : AppCompatActivity(), LocationSource, AMapLocationListener {

    private lateinit var binding: ActivityChooseLocationBinding
    private lateinit var aMap: AMap
    private var locationClient: AMapLocationClient? = null
    private var locationChangedListener: LocationSource.OnLocationChangedListener? = null

    private var currentLatitude: Double = 39.9042  // 默认北京
    private var currentLongitude: Double = 116.4074
    private var currentAddress: String = ""
    private var currentTitle: String = ""
    private var isFirstLocation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置沉浸式状态栏
        setupStatusBar()
        
        binding = ActivityChooseLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化地图
        binding.mapView.onCreate(savedInstanceState)
        
        initMap()
        initViews()
        initLocation()
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility = 
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    private fun initMap() {
        aMap = binding.mapView.map
        
        // 设置地图UI
        aMap.uiSettings.apply {
            isZoomControlsEnabled = false  // 隐藏缩放按钮
            isMyLocationButtonEnabled = false  // 使用自定义定位按钮
            isCompassEnabled = false
            isScaleControlsEnabled = true
        }
        
        // 设置定位蓝点样式
        val myLocationStyle = MyLocationStyle().apply {
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
            interval(2000)
            strokeColor(Color.TRANSPARENT)
            radiusFillColor(Color.parseColor("#1A0091EA"))
            myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_dot))
        }
        aMap.myLocationStyle = myLocationStyle
        aMap.isMyLocationEnabled = true
        aMap.setLocationSource(this)
        
        // 设置默认缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16f))
        
        // 监听地图移动
        aMap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(position: CameraPosition?) {}

            override fun onCameraChangeFinish(position: CameraPosition?) {
                position?.let {
                    currentLatitude = it.target.latitude
                    currentLongitude = it.target.longitude
                    reverseGeocode(currentLatitude, currentLongitude)
                }
            }
        })
        
        // 设置状态栏占位高度
        ViewCompat.setOnApplyWindowInsetsListener(binding.statusBarView) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val params = v.layoutParams
            params.height = statusBarHeight
            v.layoutParams = params
            insets
        }
    }

    private fun initViews() {
        binding.backIv.setOnClickListener { finish() }

        binding.myLocationBtn.setOnClickListener {
            // 移动到当前位置
            locationClient?.startLocation()
        }

        binding.confirmBtn.setOnClickListener {
            if (currentAddress.isNotEmpty()) {
                WKMapApplication.getInstance().locationCallback?.onLocationSelected(
                    currentAddress,
                    currentTitle,
                    currentLatitude,
                    currentLongitude
                )
                finish()
            } else {
                Toast.makeText(this, R.string.getting_location, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "缺少定位权限", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 设置隐私合规
            AMapLocationClient.updatePrivacyShow(this, true, true)
            AMapLocationClient.updatePrivacyAgree(this, true)
            
            locationClient = AMapLocationClient(applicationContext)
            
            val locationOption = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                isNeedAddress = true
                isOnceLocation = false
                isWifiScan = true
                isMockEnable = false
                interval = 2000
            }
            
            locationClient?.setLocationOption(locationOption)
            locationClient?.setLocationListener(this)
            locationClient?.startLocation()
            
            showLoading()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "定位初始化失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reverseGeocode(latitude: Double, longitude: Double) {
        binding.addressTitleTv.text = getString(R.string.getting_location)
        binding.addressDetailTv.text = ""
        
        Thread {
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                
                runOnUiThread {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        currentTitle = address.featureName 
                            ?: address.subLocality 
                            ?: address.locality 
                            ?: "当前位置"
                        currentAddress = address.getAddressLine(0) ?: ""
                        
                        binding.addressTitleTv.text = currentTitle
                        binding.addressDetailTv.text = if (currentAddress.isNotEmpty()) {
                            currentAddress
                        } else {
                            String.format("经纬度: %.6f, %.6f", latitude, longitude)
                        }
                    } else {
                        currentTitle = "当前位置"
                        currentAddress = String.format("%.6f, %.6f", latitude, longitude)
                        binding.addressTitleTv.text = currentTitle
                        binding.addressDetailTv.text = "无法获取详细地址"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    currentTitle = "当前位置"
                    currentAddress = String.format("%.6f, %.6f", latitude, longitude)
                    binding.addressTitleTv.text = currentTitle
                    binding.addressDetailTv.text = currentAddress
                }
            }
        }.start()
    }

    override fun onLocationChanged(location: AMapLocation?) {
        if (location == null) return
        
        hideLoading()
        
        if (location.errorCode == 0) {
            currentLatitude = location.latitude
            currentLongitude = location.longitude
            
            // 通知地图更新位置蓝点
            locationChangedListener?.onLocationChanged(location)
            
            // 首次定位时移动到当前位置
            if (isFirstLocation) {
                isFirstLocation = false
                aMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(currentLatitude, currentLongitude),
                        16f
                    )
                )
            }
        } else {
            Toast.makeText(this, "定位失败: ${location.errorInfo}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        locationChangedListener = listener
    }

    override fun deactivate() {
        locationChangedListener = null
    }

    private fun showLoading() {
        binding.loadingLayout.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingLayout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        locationClient?.stopLocation()
        locationClient?.onDestroy()
    }
}
