package com.chat.map

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.chat.map.databinding.ActivityShowLocationBinding

/**
 * 位置展示页面 - 使用高德地图SDK
 */
class ShowLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowLocationBinding
    private lateinit var aMap: AMap
    
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var address: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置沉浸式状态栏
        setupStatusBar()
        
        binding = ActivityShowLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 获取传入的位置信息
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        address = intent.getStringExtra("address") ?: ""
        title = intent.getStringExtra("title") ?: ""
        
        // 初始化地图
        binding.mapView.onCreate(savedInstanceState)
        
        initMapView()
        initViews()
    }
    
    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility = 
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }
    
    private fun initMapView() {
        aMap = binding.mapView.map
        
        // 设置地图UI
        aMap.uiSettings.apply {
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = false
            isCompassEnabled = false
            isScaleControlsEnabled = true
        }
        
        // 移动到目标位置
        val position = LatLng(latitude, longitude)
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16f))
        
        // 添加标记
        val markerOptions = MarkerOptions()
            .position(position)
            .title(title)
            .snippet(address)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin_large))
        aMap.addMarker(markerOptions)
        
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
        
        // 显示位置信息
        binding.titleTv.text = title.ifEmpty { "位置" }
        binding.addressTv.text = address.ifEmpty { String.format("%.6f, %.6f", latitude, longitude) }
        
        // 导航按钮 - 打开系统地图应用
        binding.navigateBtn.setOnClickListener {
            openNavigation()
        }
    }
    
    private fun openNavigation() {
        try {
            // 优先尝试高德地图
            val amapUri = Uri.parse("amapuri://route/plan/?dlat=$latitude&dlon=$longitude&dname=${Uri.encode(title)}&dev=0&t=0")
            val amapIntent = Intent(Intent.ACTION_VIEW, amapUri)
            amapIntent.setPackage("com.autonavi.minimap")
            if (amapIntent.resolveActivity(packageManager) != null) {
                startActivity(amapIntent)
                return
            }
            
            // 尝试百度地图
            val baiduUri = Uri.parse("baidumap://map/direction?destination=latlng:$latitude,$longitude|name:${Uri.encode(title)}&coord_type=wgs84&mode=driving")
            val baiduIntent = Intent(Intent.ACTION_VIEW, baiduUri)
            baiduIntent.setPackage("com.baidu.BaiduMap")
            if (baiduIntent.resolveActivity(packageManager) != null) {
                startActivity(baiduIntent)
                return
            }
            
            // 尝试腾讯地图
            val qqmapUri = Uri.parse("qqmap://map/routeplan?type=drive&to=${Uri.encode(title)}&tocoord=$latitude,$longitude&referer=wkchat")
            val qqmapIntent = Intent(Intent.ACTION_VIEW, qqmapUri)
            qqmapIntent.setPackage("com.tencent.map")
            if (qqmapIntent.resolveActivity(packageManager) != null) {
                startActivity(qqmapIntent)
                return
            }
            
            // 最后使用通用的 geo URI
            val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(title)})")
            val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)
            if (geoIntent.resolveActivity(packageManager) != null) {
                startActivity(geoIntent)
            } else {
                Toast.makeText(this, "未找到可用的地图应用", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "打开地图失败", Toast.LENGTH_SHORT).show()
        }
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
    }
}
