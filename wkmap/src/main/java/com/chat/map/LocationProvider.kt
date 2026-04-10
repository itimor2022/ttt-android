package com.chat.map

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.chat.base.msgitem.WKChatBaseProvider
import com.chat.base.msgitem.WKChatIteMsgFromType
import com.chat.base.msgitem.WKContentType
import com.chat.base.msgitem.WKUIChatMsgItemEntity

/**
 * 位置消息展示 Provider
 */
class LocationProvider : WKChatBaseProvider() {

    override val itemViewType: Int
        get() = WKContentType.WK_LOCATION

    override fun getChatViewItem(parentView: ViewGroup, from: WKChatIteMsgFromType): View? {
        return LayoutInflater.from(context).inflate(R.layout.item_location_msg, parentView, false)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: WKUIChatMsgItemEntity,
        from: WKChatIteMsgFromType
    ) {
        val msg = uiChatMsgItemEntity.wkMsg
        val content = msg.baseContentMsgModel as? LocationContent ?: return

        val containerLayout = parentView.findViewById<LinearLayout>(R.id.containerLayout)
        val titleTv = parentView.findViewById<TextView>(R.id.titleTv)
        val addressTv = parentView.findViewById<TextView>(R.id.addressTv)

        // 设置标题和地址
        titleTv.text = content.title.ifEmpty { 
            String.format("%.4f, %.4f", content.latitude, content.longitude) 
        }
        addressTv.text = content.address

        // 设置背景 - 根据发送/接收方设置不同背景
        val bgResId = if (from == WKChatIteMsgFromType.SEND) {
            R.drawable.location_msg_bg_send
        } else {
            R.drawable.location_msg_bg_receive
        }
        containerLayout.setBackgroundResource(bgResId)

        // 点击查看位置详情
        containerLayout.setOnClickListener {
            openLocationDetail(content)
        }
    }

    private fun openLocationDetail(content: LocationContent) {
        val intent = Intent(context, ShowLocationActivity::class.java)
        intent.putExtra("latitude", content.latitude)
        intent.putExtra("longitude", content.longitude)
        intent.putExtra("address", content.address)
        intent.putExtra("title", content.title)
        context.startActivity(intent)
    }

    override fun resetCellBackground(
        parentView: View, 
        uiChatMsgItemEntity: WKUIChatMsgItemEntity, 
        from: WKChatIteMsgFromType
    ) {
        val containerLayout = parentView.findViewById<LinearLayout>(R.id.containerLayout)
        val bgResId = if (from == WKChatIteMsgFromType.SEND) {
            R.drawable.location_msg_bg_send
        } else {
            R.drawable.location_msg_bg_receive
        }
        containerLayout.setBackgroundResource(bgResId)
    }

    override fun resetCellListener(
        position: Int,
        parentView: View, 
        uiChatMsgItemEntity: WKUIChatMsgItemEntity, 
        from: WKChatIteMsgFromType
    ) {
        val msg = uiChatMsgItemEntity.wkMsg
        val content = msg.baseContentMsgModel as? LocationContent ?: return
        val containerLayout = parentView.findViewById<LinearLayout>(R.id.containerLayout)
        
        containerLayout.setOnClickListener {
            openLocationDetail(content)
        }
    }
}
