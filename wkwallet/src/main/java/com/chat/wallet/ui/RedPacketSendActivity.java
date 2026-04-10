package com.chat.wallet.ui;

import android.content.Intent;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivityRedPacketSendBinding;
import com.chat.wallet.msgitem.RedPacketContent;
import com.chat.wallet.service.WalletModel;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannelType;

import java.math.BigDecimal;

/**
 * 发红包页面 - 商业级别UI
 */
public class RedPacketSendActivity extends WKBaseActivity<ActivityRedPacketSendBinding> {
    
    private String channelId;
    private int channelType;
    private int packetType = 1; // 1:普通红包 2:拼手气红包
    
    @Override
    protected ActivityRedPacketSendBinding getViewBinding() {
        return ActivityRedPacketSendBinding.inflate(getLayoutInflater());
    }
    
    @Override
    protected void setTitle(TextView titleTv) {
        // 使用自定义标题栏，不需要默认标题
    }
    
    @Override
    protected void initPresenter() {
        // 使用自定义标题栏，不需要默认标题栏
    }
    
    @Override
    protected void initData() {
        channelId = getIntent().getStringExtra("channel_id");
        channelType = getIntent().getIntExtra("channel_type", WKChannelType.PERSONAL);
        
        // 个人聊天隐藏红包数量和类型选择
        if (channelType == WKChannelType.PERSONAL) {
            wkVBinding.countLayout.setVisibility(View.GONE);
            wkVBinding.typeLayout.setVisibility(View.GONE);
        } else {
            wkVBinding.countLayout.setVisibility(View.VISIBLE);
            wkVBinding.typeLayout.setVisibility(View.VISIBLE);
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
        
        // 红包类型切换
        wkVBinding.normalTypeBtn.setOnClickListener(v -> {
            packetType = 1;
            wkVBinding.normalTypeBtn.setBackgroundResource(R.drawable.bg_red_type_selected);
            wkVBinding.normalTypeBtn.setTextColor(0xFFC62828);
            wkVBinding.luckyTypeBtn.setBackgroundColor(0x00000000);
            wkVBinding.luckyTypeBtn.setTextColor(0xFF999999);
        });
        
        wkVBinding.luckyTypeBtn.setOnClickListener(v -> {
            packetType = 2;
            wkVBinding.luckyTypeBtn.setBackgroundResource(R.drawable.bg_red_type_selected);
            wkVBinding.luckyTypeBtn.setTextColor(0xFFC62828);
            wkVBinding.normalTypeBtn.setBackgroundColor(0x00000000);
            wkVBinding.normalTypeBtn.setTextColor(0xFF999999);
        });
        
        // 金额输入监听
        wkVBinding.amountEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // 限制小数点后两位
                String text = s.toString();
                if (text.contains(".")) {
                    int dotIndex = text.indexOf(".");
                    if (text.length() - dotIndex > 3) {
                        s.delete(dotIndex + 3, text.length());
                    }
                }
            }
        });
        
        wkVBinding.submitBtn.setOnClickListener(v -> submitRedPacket());
    }
    
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(0xFFFFF8F0);
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
                String balance = String.format("钱包余额 ¥%.2f", data.balance / 100.0);
                wkVBinding.balanceTv.setText(balance);
            }
        });
    }
    
    private void submitRedPacket() {
        String amountStr = wkVBinding.amountEt.getText().toString().trim();
        String countStr = wkVBinding.countEt.getText().toString().trim();
        String remark = wkVBinding.remarkEt.getText().toString().trim();
        
        if (TextUtils.isEmpty(amountStr)) {
            showToast("请输入红包金额");
            return;
        }
        
        int count = 1;
        if (channelType != WKChannelType.PERSONAL) {
            if (TextUtils.isEmpty(countStr)) {
                showToast("请输入红包数量");
                return;
            }
            try {
                count = Integer.parseInt(countStr);
                if (count <= 0) {
                    showToast("红包数量必须大于0");
                    return;
                }
            } catch (NumberFormatException e) {
                showToast("请输入有效的红包数量");
                return;
            }
        }
        
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showToast("红包金额必须大于0");
                return;
            }
            
            // 单个红包最低金额
            if (amount.compareTo(new BigDecimal("0.01")) < 0) {
                showToast("红包金额最少0.01元");
                return;
            }
            
            long amountFen = amount.multiply(new BigDecimal(100)).longValue();
            
            if (TextUtils.isEmpty(remark)) {
                remark = getString(R.string.default_red_packet_remark);
            }
            
            final int finalCount = count;
            final String finalRemark = remark;
            final long finalAmountFen = amountFen;
            final String finalAmountStr = amountStr;
            
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
                    startActivity(new Intent(RedPacketSendActivity.this, RealNameVerifyActivity.class));
                    return;
                }
                
                // 检查支付密码
                if (!walletInfo.has_pay_pwd) {
                    showToast("请先设置支付密码");
                    SetPayPasswordDialog dialog = new SetPayPasswordDialog(RedPacketSendActivity.this);
                    dialog.setOnPasswordSetListener(() -> {
                        // 密码设置成功后，重新提交
                        submitRedPacket();
                    });
                    dialog.show();
                    return;
                }
                
                // 弹出支付密码对话框
                PayPasswordDialog dialog = new PayPasswordDialog(RedPacketSendActivity.this, password -> {
                    loadingPopup.show();
                    WalletModel.getInstance().sendRedPacket(channelId, channelType, packetType, finalAmountFen, 
                        finalCount, finalRemark, password, (code1, msg1, detail) -> {
                        loadingPopup.dismiss();
                        if (code1 == HttpResponseCode.success && detail != null) {
                            // 发送红包消息
                            sendRedPacketMessage(detail.packet_no, finalAmountFen, finalRemark);
                            showToast("红包发送成功");
                            finish();
                        } else {
                            showToast(msg1);
                        }
                    });
                });
                dialog.setTitle("发红包");
                dialog.setAmount("¥" + finalAmountStr);
                dialog.show();
            });
        } catch (NumberFormatException e) {
            showToast("请输入有效金额");
        }
    }
    
    private void sendRedPacketMessage(String packetNo, long amount, String remark) {
        RedPacketContent content = new RedPacketContent();
        content.packetNo = packetNo;
        content.amount = amount;
        content.remark = remark;
        
        WKIM.getInstance().getMsgManager().sendMessage(content, channelId, (byte) channelType);
    }
}
