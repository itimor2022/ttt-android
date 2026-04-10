package com.chat.map

import android.content.Context
import android.content.Intent
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.ChatFunctionMenu
import com.chat.base.endpoint.entity.ChooseLocationMenu
import com.chat.base.endpoint.entity.LocationMenu
import com.chat.base.msg.IConversationContext
import com.chat.base.msgitem.WKContentType
import com.chat.base.msgitem.WKMsgItemViewManager
import com.xinbida.wukongim.WKIM
import java.lang.ref.WeakReference

/**
 * 地图模块入口
 */
class WKMapApplication private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: WKMapApplication? = null

        @JvmStatic
        fun getInstance(): WKMapApplication {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WKMapApplication().also { INSTANCE = it }
            }
        }
    }

    private var contextRef: WeakReference<Context>? = null
    
    // 位置选择回调
    var locationCallback: ILocationCallback? = null

    fun init(context: Context) {
        this.contextRef = WeakReference(context)
        
        // 注册位置消息类型
        WKIM.getInstance().msgManager.registerContentMsg(LocationContent::class.java)
        
        // 注册位置消息展示 Provider
        WKMsgItemViewManager.getInstance().addChatItemViewProvider(
            WKContentType.WK_LOCATION,
            LocationProvider()
        )
        
        // 注册选择位置 endpoint（朋友圈使用）
        registerChooseLocation()
        
        // 注册显示位置 endpoint
        registerShowLocation()
        
        // 注册聊天功能面板入口
        // registerChatFunction(context) // 删除发送位置按钮
    }

    private fun registerChooseLocation() {
        EndpointManager.getInstance().setMethod("choose_location") { obj ->
            val menu = obj as? ChooseLocationMenu ?: return@setMethod null
            locationCallback = object : ILocationCallback {
                override fun onLocationSelected(address: String, title: String, latitude: Double, longitude: Double) {
                    menu.iBack.onResult(address, title, latitude, longitude)
                }
            }
            val intent = Intent(menu.context, ChooseLocationActivity::class.java)
            menu.context.startActivity(intent)
            null
        }
    }

    private fun registerShowLocation() {
        EndpointManager.getInstance().setMethod("show_location") { obj ->
            val menu = obj as? LocationMenu ?: return@setMethod null
            val intent = Intent(menu.context, ShowLocationActivity::class.java)
            intent.putExtra("latitude", menu.latitude)
            intent.putExtra("longitude", menu.longitude)
            intent.putExtra("address", menu.address)
            intent.putExtra("title", menu.address)
            menu.context.startActivity(intent)
            null
        }
    }

    private fun registerChatFunction(context: Context) {
        // 在聊天功能面板添加位置入口
        EndpointManager.getInstance().setMethod(
            EndpointCategory.chatFunction + "_location",
            EndpointCategory.chatFunction,
            93  // 排在文件之后
        ) { obj ->
            ChatFunctionMenu(
                "location",
                R.drawable.ic_location,
                context.getString(R.string.send_location),
                ChatFunctionMenu.IChatFunctionCLick { conversationContext ->
                    sendLocation(conversationContext)
                }
            )
        }
    }

    private fun sendLocation(conversationContext: IConversationContext) {
        locationCallback = object : ILocationCallback {
            override fun onLocationSelected(address: String, title: String, latitude: Double, longitude: Double) {
                // 发送位置消息
                val locationContent = LocationContent()
                locationContent.address = address
                locationContent.title = title
                locationContent.latitude = latitude
                locationContent.longitude = longitude
                
                conversationContext.sendMessage(locationContent)
            }
        }
        
        val intent = Intent(conversationContext.chatActivity, ChooseLocationActivity::class.java)
        conversationContext.chatActivity.startActivity(intent)
    }

    interface ILocationCallback {
        fun onLocationSelected(address: String, title: String, latitude: Double, longitude: Double)
    }
}

