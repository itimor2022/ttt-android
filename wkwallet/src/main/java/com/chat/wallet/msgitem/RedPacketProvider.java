package com.chat.wallet.msgitem;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chat.base.msgitem.WKChatBaseProvider;
import com.chat.base.msgitem.WKChatIteMsgFromType;
import com.chat.base.msgitem.WKContentType;
import com.chat.base.msgitem.WKUIChatMsgItemEntity;
import com.chat.wallet.R;
import com.chat.wallet.ui.RedPacketOpenActivity;

/**
 * 红包消息提供者
 */
public class RedPacketProvider extends WKChatBaseProvider {
    
    @NonNull
    @Override
    protected View getChatViewItem(@NonNull ViewGroup parentView, @NonNull WKChatIteMsgFromType from) {
        return LayoutInflater.from(getContext()).inflate(R.layout.item_msg_red_packet, parentView, false);
    }
    
    @Override
    protected void setData(int adapterPosition, @NonNull View parentView, @NonNull WKUIChatMsgItemEntity uiChatMsgItemEntity, @NonNull WKChatIteMsgFromType from) {
        RedPacketContent content = (RedPacketContent) uiChatMsgItemEntity.wkMsg.baseContentMsgModel;
        if (content == null) {
            android.util.Log.e("RedPacketProvider", "RedPacketContent is null!");
            return;
        }
        
        // 调试日志
        android.util.Log.d("RedPacketProvider", "RedPacketContent: packetNo=" + content.packetNo + 
            ", amount=" + content.amount + ", remark=" + content.remark + ", status=" + content.status);
        
        LinearLayout containerLayout = parentView.findViewById(R.id.containerLayout);
        TextView remarkTv = parentView.findViewById(R.id.remarkTv);
        TextView statusTv = parentView.findViewById(R.id.statusTv);
        
        // 设置祝福语
        if (remarkTv != null) {
            remarkTv.setText(content.remark);
        }
        
        // 根据红包状态设置样式
        if (content.status == 1) {
            // 已领取 - 使用灰色背景
            containerLayout.setBackgroundResource(R.drawable.bg_red_packet_msg_grabbed);
            if (statusTv != null) {
                statusTv.setVisibility(View.VISIBLE);
                statusTv.setText(R.string.red_packet_grabbed);
            }
        } else if (content.status == 2) {
            // 红包已抢完
            containerLayout.setBackgroundResource(R.drawable.bg_red_packet_msg_grabbed);
            if (statusTv != null) {
                statusTv.setVisibility(View.VISIBLE);
                statusTv.setText(R.string.red_packet_finished);
            }
        } else if (content.status == 3) {
            // 红包已过期
            containerLayout.setBackgroundResource(R.drawable.bg_red_packet_msg_grabbed);
            if (statusTv != null) {
                statusTv.setVisibility(View.VISIBLE);
                statusTv.setText(R.string.red_packet_expired);
            }
        } else {
            // 未领取 - 使用红色背景
            containerLayout.setBackgroundResource(R.drawable.bg_red_packet_msg);
            if (statusTv != null) {
                statusTv.setVisibility(View.GONE);
            }
        }
        
        // 点击事件
        containerLayout.setOnClickListener(v -> {
            if (getContext() == null) return;
            android.util.Log.d("RedPacketProvider", "onClick: packetNo=" + content.packetNo);
            Intent intent = new Intent(getContext(), RedPacketOpenActivity.class);
            intent.putExtra("packet_no", content.packetNo);
            intent.putExtra("from_uid", uiChatMsgItemEntity.wkMsg.fromUID);
            intent.putExtra("remark", content.remark);
            intent.putExtra("status", content.status);
            getContext().startActivity(intent);
        });
        
        addLongClick(parentView, uiChatMsgItemEntity);
    }
    
    @Override
    public int getItemViewType() {
        return WKContentType.redPacket;
    }
    
    @Override
    public void resetCellBackground(@NonNull View parentView, @NonNull WKUIChatMsgItemEntity uiChatMsgItemEntity, @NonNull WKChatIteMsgFromType from) {
        // 红包消息有自己的背景，不需要重置
    }
    
    @Override
    public void resetCellListener(int position, @NonNull View parentView, @NonNull WKUIChatMsgItemEntity uiChatMsgItemEntity, @NonNull WKChatIteMsgFromType from) {
        RedPacketContent content = (RedPacketContent) uiChatMsgItemEntity.wkMsg.baseContentMsgModel;
        if (content == null) {
            android.util.Log.e("RedPacketProvider", "resetCellListener: content is null!");
            return;
        }
        
        android.util.Log.d("RedPacketProvider", "resetCellListener: packetNo=" + content.packetNo);
        
        LinearLayout containerLayout = parentView.findViewById(R.id.containerLayout);
        containerLayout.setOnClickListener(v -> {
            if (getContext() == null) return;
            android.util.Log.d("RedPacketProvider", "onClick (from resetCellListener): packetNo=" + content.packetNo);
            Intent intent = new Intent(getContext(), RedPacketOpenActivity.class);
            intent.putExtra("packet_no", content.packetNo);
            intent.putExtra("from_uid", uiChatMsgItemEntity.wkMsg.fromUID);
            intent.putExtra("remark", content.remark);
            intent.putExtra("status", content.status);
            getContext().startActivity(intent);
        });
    }
}
