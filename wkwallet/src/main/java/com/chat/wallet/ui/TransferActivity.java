package com.chat.wallet.ui;

import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.glide.GlideUtils;
import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivityTransferBinding;
import com.chat.wallet.msgitem.TransferContent;
import com.chat.wallet.service.WalletModel;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelType;

import java.math.BigDecimal;

/**
 * 转账页面
 */
public class TransferActivity extends WKBaseActivity<ActivityTransferBinding> {
    
    private String toUid;
    private String toName;
    
    @Override
    protected ActivityTransferBinding getViewBinding() {
        return ActivityTransferBinding.inflate(getLayoutInflater());
    }
    
    @Override
    protected void setTitle(TextView titleTv) {
        // 使用自定义标题栏
    }
    
    @Override
    protected void initPresenter() {
        // 使用自定义标题栏
    }
    
    @Override
    protected void initData() {
        toUid = getIntent().getStringExtra("to_uid");
        toName = getIntent().getStringExtra("to_name");
        
        if (TextUtils.isEmpty(toUid)) {
            showToast("参数错误");
            finish();
            return;
        }
        
        wkVBinding.toNameTv.setText(toName);
        
        // 加载头像
        WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(toUid, WKChannelType.PERSONAL);
        if (channel != null) {
            GlideUtils.getInstance().showAvatarImg(this, channel.channelID, WKChannelType.PERSONAL, channel.avatarCacheKey, wkVBinding.avatarIv);
        }
        
        // 加载余额
        loadBalance();
    }
    
    @Override
    protected void initView() {
        // 设置状态栏
        setupStatusBar();
        
        // 返回按钮
        wkVBinding.backIv.setOnClickListener(v -> finish());
        
        wkVBinding.submitBtn.setOnClickListener(v -> submitTransfer());
    }
    
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(0xFFF0F5FF);
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(wkVBinding.statusBarView, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = statusBarHeight;
            v.setLayoutParams(params);
            return insets;
        });
    }
    
    private void loadBalance() {
        WalletModel.getInstance().getWalletInfo((code, msg, data) -> {
            if (code == HttpResponseCode.success && data != null) {
                String balance = String.format("¥%.2f", data.balance / 100.0);
                wkVBinding.balanceTv.setText(balance);
            }
        });
    }
    
    private void submitTransfer() {
        String amountStr = wkVBinding.amountEt.getText().toString().trim();
        String remark = wkVBinding.remarkEt.getText().toString().trim();
        
        if (TextUtils.isEmpty(amountStr)) {
            showToast("请输入转账金额");
            return;
        }
        
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showToast("转账金额必须大于0");
                return;
            }
            
            long amountFen = amount.multiply(new BigDecimal(100)).longValue();
            
            // 检查实名认证状态
            WalletModel.getInstance().getWalletInfo((code, msg, walletInfo) -> {
                if (code != HttpResponseCode.success || walletInfo == null) {
                    showToast("加载钱包信息失败");
                    return;
                }
                
                if (walletInfo.isRealNameAuditing()) {
                    showToast("实名认证正在审核中，请耐心等待");
                    return;
                }
                
                if (walletInfo.needRealNameVerify()) {
                    showToast("请先完成实名认证");
                    startActivity(new Intent(TransferActivity.this, RealNameVerifyActivity.class));
                    return;
                }
                
                // 检查支付密码
                if (!walletInfo.has_pay_pwd) {
                    showToast("请先设置支付密码");
                    SetPayPasswordDialog dialog = new SetPayPasswordDialog(TransferActivity.this);
                    dialog.setOnPasswordSetListener(() -> {
                        // 密码设置成功后，重新提交
                        submitTransfer();
                    });
                    dialog.show();
                    return;
                }
                
                // 弹出支付密码对话框
                PayPasswordDialog dialog = new PayPasswordDialog(TransferActivity.this, password -> {
                    loadingPopup.show();
                    WalletModel.getInstance().sendTransfer(toUid, amountFen, remark, password, (code1, msg1, detail) -> {
                        loadingPopup.dismiss();
                        if (code1 == HttpResponseCode.success && detail != null) {
                            // 发送转账消息
                            sendTransferMessage(detail.transfer_no, amountFen, remark);
                            showToast("转账成功");
                            finish();
                        } else {
                            showToast(msg1);
                        }
                    });
                });
                dialog.setTitle("转账");
                dialog.setAmount("¥" + amountStr);
                dialog.show();
            });
        } catch (NumberFormatException e) {
            showToast("请输入有效金额");
        }
    }
    
    private void sendTransferMessage(String transferNo, long amount, String remark) {
        TransferContent content = new TransferContent();
        content.transferNo = transferNo;
        content.amount = amount;
        content.remark = remark;
        content.status = 0;
        
        WKIM.getInstance().getMsgManager().sendMessage(content, toUid, WKChannelType.PERSONAL);
    }
}

