package com.chat.wallet.ui;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKConfig;
import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivityTransferDetailBinding;
import com.chat.wallet.entity.TransferDetail;
import com.chat.wallet.msgitem.TransferContent;
import com.chat.wallet.service.WalletModel;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKMsg;

/**
 * 转账详情页面
 */
public class TransferDetailActivity extends WKBaseActivity<ActivityTransferDetailBinding> {
    
    private String transferNo;
    private String clientMsgNO;
    private TransferDetail detail;
    
    @Override
    protected ActivityTransferDetailBinding getViewBinding() {
        return ActivityTransferDetailBinding.inflate(getLayoutInflater());
    }
    
    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText("转账详情");
    }
    
    @Override
    protected void initView() {
        // 先获取参数（在 initData() 之前执行）
        transferNo = getIntent().getStringExtra("transfer_no");
        clientMsgNO = getIntent().getStringExtra("client_msg_no");
        android.util.Log.d("TransferDetail", "initView: transferNo=" + transferNo + ", clientMsgNO=" + clientMsgNO);
        
        if (TextUtils.isEmpty(transferNo)) {
            android.util.Log.e("TransferDetail", "transferNo is empty, finishing activity");
            showToast("转账不存在");
            finish();
            return;
        }
        
        wkVBinding.receiveBtn.setOnClickListener(v -> receiveTransfer());
        wkVBinding.refundBtn.setOnClickListener(v -> refundTransfer());
        
        loadDetail();
    }
    
    @Override
    protected void initData() {
        // 参数已在 initView() 中获取
    }
    
    private void loadDetail() {
        android.util.Log.d("TransferDetail", "loadDetail called, transferNo=" + transferNo);
        WalletModel.getInstance().getTransferDetail(transferNo, (code, msg, result) -> {
            android.util.Log.d("TransferDetail", "API response: code=" + code + ", msg=" + msg + ", result=" + (result != null ? "not null" : "null"));
            if (code == HttpResponseCode.success && result != null) {
                detail = result;
                updateUI();
            } else {
                android.util.Log.e("TransferDetail", "API failed: " + msg);
                showToast(msg);
            }
        });
    }
    
    private void updateUI() {
        if (detail == null) return;
        
        // 调试日志
        android.util.Log.d("TransferDetail", "TransferDetail: transfer_no=" + detail.transfer_no +
            ", amount=" + detail.amount + ", remark=" + detail.remark + ", status=" + detail.status);
        
        wkVBinding.amountTv.setText("¥" + detail.getAmountYuan());
        wkVBinding.remarkTv.setText(TextUtils.isEmpty(detail.remark) ? "转账" : detail.remark);
        wkVBinding.statusTv.setText(detail.getStatusName());
        wkVBinding.timeTv.setText(detail.created_at);
        
        // 根据当前用户和状态显示操作按钮
        String myUid = WKConfig.getInstance().getUid();
        boolean isReceiver = myUid.equals(detail.to_uid);
        
        if (detail.canReceive()) {
            if (isReceiver) {
                // 我是接收方，显示领取和退回按钮
                wkVBinding.actionLayout.setVisibility(View.VISIBLE);
                wkVBinding.receiveBtn.setVisibility(View.VISIBLE);
                wkVBinding.refundBtn.setVisibility(View.VISIBLE);
            } else {
                // 我是发送方，显示退回按钮
                wkVBinding.actionLayout.setVisibility(View.VISIBLE);
                wkVBinding.receiveBtn.setVisibility(View.GONE);
                wkVBinding.refundBtn.setVisibility(View.VISIBLE);
            }
        } else {
            // 已处理，隐藏操作按钮
            wkVBinding.actionLayout.setVisibility(View.GONE);
        }
    }
    
    private void receiveTransfer() {
        loadingPopup.show();
        WalletModel.getInstance().receiveTransfer(transferNo, (code, msg) -> {
            loadingPopup.dismiss();
            if (code == HttpResponseCode.success) {
                showToast("领取成功");
                // 更新本地消息状态
                updateLocalMsgStatus(1); // 1 = 已领取
                loadDetail();
            } else {
                showToast(msg);
            }
        });
    }
    
    private void updateLocalMsgStatus(int newStatus) {
        if (TextUtils.isEmpty(clientMsgNO)) {
            android.util.Log.w("TransferDetail", "clientMsgNO is empty, cannot update local msg");
            return;
        }
        
        try {
            WKMsg wkMsg = WKIM.getInstance().getMsgManager().getWithClientMsgNO(clientMsgNO);
            if (wkMsg != null && wkMsg.baseContentMsgModel instanceof TransferContent) {
                TransferContent content = (TransferContent) wkMsg.baseContentMsgModel;
                content.status = newStatus;
                // 更新消息内容并刷新UI
                WKIM.getInstance().getMsgManager().updateContentAndRefresh(clientMsgNO, content, false);
                android.util.Log.d("TransferDetail", "Updated local msg status to " + newStatus);
            }
        } catch (Exception e) {
            android.util.Log.e("TransferDetail", "Failed to update local msg: " + e.getMessage());
        }
    }
    
    private void refundTransfer() {
        loadingPopup.show();
        WalletModel.getInstance().refundTransfer(transferNo, (code, msg) -> {
            loadingPopup.dismiss();
            if (code == HttpResponseCode.success) {
                showToast("退回成功");
                // 更新本地消息状态
                updateLocalMsgStatus(2); // 2 = 已退回
                loadDetail();
            } else {
                showToast(msg);
            }
        });
    }
}

