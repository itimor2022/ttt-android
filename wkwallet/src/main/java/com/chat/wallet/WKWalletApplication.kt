package com.chat.wallet

import android.content.Context
import android.content.Intent
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.ChatFunctionMenu
import com.chat.base.endpoint.entity.PersonalInfoMenu
import com.chat.base.msgitem.WKContentType
import com.chat.base.msgitem.WKMsgItemViewManager
import com.chat.wallet.msgitem.RedPacketContent
import com.chat.wallet.msgitem.RedPacketProvider
import com.chat.wallet.msgitem.TransferContent
import com.chat.wallet.msgitem.TransferProvider
import com.xinbida.wukongim.WKIM
import com.chat.wallet.ui.RedPacketDetailActivity
import com.chat.wallet.ui.RedPacketOpenActivity
import com.chat.wallet.ui.RedPacketSendActivity
import com.chat.wallet.ui.TransferActivity
import com.chat.wallet.ui.WalletActivity
import com.xinbida.wukongim.entity.WKChannelType
import java.lang.ref.WeakReference

/**
 * 钱包模块入口
 */
class WKWalletApplication private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: WKWalletApplication? = null
        
        @JvmStatic
        fun getInstance(): WKWalletApplication {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WKWalletApplication().also { INSTANCE = it }
            }
        }
        
        // 消息类型常量
        const val MSG_TYPE_RED_PACKET = WKContentType.redPacket
        const val MSG_TYPE_RED_PACKET_RECEIVED = WKContentType.redPacketReceived
        const val MSG_TYPE_TRANSFER = WKContentType.transfer
        const val MSG_TYPE_TRANSFER_STATUS_CHANGED = WKContentType.transferStatusChanged
    }
    
    private var contextRef: WeakReference<Context>? = null
    
    fun getContext(): Context? = contextRef?.get()
    
    fun init(context: Context) {
        this.contextRef = WeakReference(context)
        // 注册消息类型
        registerMsgProviders()
        // 注册聊天功能面板入口
        registerChatFunctions()
        // 注册个人中心入口
        registerPersonalCenter()
    }
    
    /**
     * 注册消息 Provider
     */
    private fun registerMsgProviders() {
        // 注册红包消息内容类型到WKIM
        WKIM.getInstance().msgManager.registerContentMsg(RedPacketContent::class.java)
        // 注册红包消息UI展示
        WKMsgItemViewManager.getInstance().addChatItemViewProvider(
            MSG_TYPE_RED_PACKET,
            RedPacketProvider()
        )
        
        // 注册转账消息内容类型到WKIM
        WKIM.getInstance().msgManager.registerContentMsg(TransferContent::class.java)
        // 注册转账消息UI展示
        WKMsgItemViewManager.getInstance().addChatItemViewProvider(
            MSG_TYPE_TRANSFER,
            TransferProvider()
        )
    }
    
    /**
     * 注册聊天功能面板入口
     */
    private fun registerChatFunctions() {
        // 红包入口
        EndpointManager.getInstance().setMethod(
            EndpointCategory.chatFunction + "_redPacket",
            EndpointCategory.chatFunction,
            80
        ) { _ ->
            ChatFunctionMenu(
                "redPacket",
                R.drawable.ic_red_packet,
                "红包",
                ChatFunctionMenu.IChatFunctionCLick { conversationContext ->
                    openSendRedPacket(
                        conversationContext.chatActivity,
                        conversationContext.chatChannelInfo.channelID,
                        conversationContext.chatChannelInfo.channelType.toInt()
                    )
                }
            )
        }
        
        // 转账入口(仅个人聊天显示)
        EndpointManager.getInstance().setMethod(
            EndpointCategory.chatFunction + "_transfer",
            EndpointCategory.chatFunction,
            79
        ) { obj ->
            val iConversationContext = obj as? com.chat.base.msg.IConversationContext
            if (iConversationContext?.chatChannelInfo?.channelType == WKChannelType.PERSONAL) {
                ChatFunctionMenu(
                    "transfer",
                    R.drawable.ic_transfer,
                    "转账",
                    ChatFunctionMenu.IChatFunctionCLick { conversationContext ->
                        openTransfer(
                            conversationContext.chatActivity,
                            conversationContext.chatChannelInfo.channelID,
                            conversationContext.chatChannelInfo.channelName ?: ""
                        )
                    }
                )
            } else {
                null
            }
        }
    }
    
    /**
     * 注册个人中心入口
     */
    private fun registerPersonalCenter() {
        // 钱包入口 - 放在收藏下面（收藏的排序值是100，钱包用110）
        EndpointManager.getInstance().setMethod(
            "wallet_entry", EndpointCategory.personalCenter, 110
        ) { _ ->
            PersonalInfoMenu(
                R.drawable.ic_wallet,
                "钱包"
            ) {
                contextRef?.get()?.let { ctx ->
                    val intent = Intent(ctx, WalletActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(intent)
                }
            }
        }
    }
    
    /**
     * 打开钱包页面
     */
    fun openWallet(context: Context) {
        val intent = Intent(context, WalletActivity::class.java)
        context.startActivity(intent)
    }
    
    /**
     * 打开转账页面
     */
    fun openTransfer(context: Context, toUid: String, toName: String) {
        val intent = Intent(context, TransferActivity::class.java)
        intent.putExtra("to_uid", toUid)
        intent.putExtra("to_name", toName)
        context.startActivity(intent)
    }
    
    /**
     * 打开发红包页面
     */
    fun openSendRedPacket(context: Context, channelId: String, channelType: Int) {
        val intent = Intent(context, RedPacketSendActivity::class.java)
        intent.putExtra("channel_id", channelId)
        intent.putExtra("channel_type", channelType)
        context.startActivity(intent)
    }
    
    /**
     * 打开红包
     */
    fun openRedPacket(context: Context, packetNo: String, fromUid: String, remark: String) {
        val intent = Intent(context, RedPacketOpenActivity::class.java)
        intent.putExtra("packet_no", packetNo)
        intent.putExtra("from_uid", fromUid)
        intent.putExtra("remark", remark)
        context.startActivity(intent)
    }
    
    /**
     * 红包详情
     */
    fun openRedPacketDetail(context: Context, packetNo: String) {
        val intent = Intent(context, RedPacketDetailActivity::class.java)
        intent.putExtra("packet_no", packetNo)
        context.startActivity(intent)
    }
}
