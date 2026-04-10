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
import com.chat.wallet.ui.TransferDetailActivity;

import java.text.DecimalFormat;

/**
 * 转账消息提供者
 */
public class TransferProvider extends WKChatBaseProvider {
    
    @NonNull
    @Override
    protected View getChatViewItem(@NonNull ViewGroup parentView, @NonNull WKChatIteMsgFromType from) {
        return LayoutInflater.from(getContext()).inflate(R.layout.item_msg_transfer, parentView, false);
    }
    
    @Override
    protected void setData(int adapterPosition, @NonNull View parentView, @NonNull WKUIChatMsgItemEntity uiChatMsgItemEntity, @NonNull WKChatIteMsgFromType from) {
        TransferContent content = (TransferContent) uiChatMsgItemEntity.wkMsg.baseContentMsgModel;
        if (content == null) {
            android.util.Log.e("TransferProvider", "TransferContent is null!");
            return;
        }
        
        // 调试日志
        android.util.Log.d("TransferProvider", "TransferContent: transferNo=" + content.transferNo + 
            ", amount=" + content.amount + ", remark=" + content.remark + ", status=" + content.status);
        
        LinearLayout containerLayout = parentView.findViewById(R.id.containerLayout);
        TextView amountTv = parentView.findViewById(R.id.amountTv);
        TextView remarkTv = parentView.findViewById(R.id.remarkTv);
        TextView statusTv = parentView.findViewById(R.id.statusTv);
        
        if (amountTv != null) {
            DecimalFormat df = new DecimalFormat("0.00");
            String amountStr = df.format(content.amount / 100.0);
            amountTv.setText("¥" + amountStr);
            android.util.Log.d("TransferProvider", "Display amount: ¥" + amountStr);
        }
        
        if (remarkTv != null) {
            remarkTv.setText(content.remark.isEmpty() ? "转账" : content.remark);
        }
        
        if (statusTv != null) {
            statusTv.setText(content.getStatusText());
        }
        
        // 点击查看详情
        if (containerLayout != null) {
            String clientMsgNO = uiChatMsgItemEntity.wkMsg.clientMsgNO;
            containerLayout.setOnClickListener(v -> {
                if (getContext() == null) return;
                android.util.Log.d("TransferProvider", "onClick: transferNo=" + content.transferNo);
                Intent intent = new Intent(getContext(), TransferDetailActivity.class);
                intent.putExtra("transfer_no", content.transferNo);
                intent.putExtra("client_msg_no", clientMsgNO);
                getContext().startActivity(intent);
            });
        }
        
        addLongClick(parentView, uiChatMsgItemEntity);
    }
    
    @Override
    public int getItemViewType() {
        return WKContentType.transfer;
    }
    
    @Override
    public void resetCellBackground(@NonNull View parentView, @NonNull WKUIChatMsgItemEntity uiChatMsgItemEntity, @NonNull WKChatIteMsgFromType from) {
        // 转账消息有自己的背景，不需要重置
    }
    
    @Override
    public void resetCellListener(int position, @NonNull View parentView, @NonNull WKUIChatMsgItemEntity uiChatMsgItemEntity, @NonNull WKChatIteMsgFromType from) {
        TransferContent content = (TransferContent) uiChatMsgItemEntity.wkMsg.baseContentMsgModel;
        if (content == null) {
            android.util.Log.e("TransferProvider", "resetCellListener: content is null!");
            return;
        }
        
        android.util.Log.d("TransferProvider", "resetCellListener: transferNo=" + content.transferNo);
        
        LinearLayout containerLayout = parentView.findViewById(R.id.containerLayout);
        if (containerLayout != null) {
            String clientMsgNO = uiChatMsgItemEntity.wkMsg.clientMsgNO;
            containerLayout.setOnClickListener(v -> {
                if (getContext() == null) return;
                android.util.Log.d("TransferProvider", "onClick (from resetCellListener): transferNo=" + content.transferNo);
                Intent intent = new Intent(getContext(), TransferDetailActivity.class);
                intent.putExtra("transfer_no", content.transferNo);
                intent.putExtra("client_msg_no", clientMsgNO);
                getContext().startActivity(intent);
            });
        }
    }
}
