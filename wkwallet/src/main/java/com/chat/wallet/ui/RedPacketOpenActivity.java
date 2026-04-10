package com.chat.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chat.base.config.WKConfig;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.WKToastUtils;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivityRedPacketOpenBinding;
import com.chat.wallet.entity.RedPacketDetail;
import com.chat.wallet.service.WalletModel;

/**
 * 打开红包页面
 */
public class RedPacketOpenActivity extends AppCompatActivity {
    
    private ActivityRedPacketOpenBinding binding;
    private String packetNo;
    private String fromUid;
    private String remark;
    private RedPacketDetail detail;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置透明状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        
        binding = ActivityRedPacketOpenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        packetNo = getIntent().getStringExtra("packet_no");
        fromUid = getIntent().getStringExtra("from_uid");
        remark = getIntent().getStringExtra("remark");
        
        android.util.Log.d("RedPacketOpen", "onCreate: packetNo=" + packetNo);
        
        if (TextUtils.isEmpty(packetNo)) {
            android.util.Log.e("RedPacketOpen", "packetNo is empty, finishing activity");
            WKToastUtils.getInstance().showToastFail("红包不存在");
            finish();
            return;
        }
        
        initView();
        loadDetail();
    }
    
    private void initView() {
        binding.remarkTv.setText(TextUtils.isEmpty(remark) ? getString(R.string.default_red_packet_remark) : remark);
        
        binding.closeBtn.setOnClickListener(v -> finish());
        
        binding.openBtn.setOnClickListener(v -> grabRedPacket());
        
        binding.detailBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, RedPacketDetailActivity.class);
            intent.putExtra("packet_no", packetNo);
            startActivity(intent);
            finish();
        });
    }
    
    private void loadDetail() {
        android.util.Log.d("RedPacketOpen", "loadDetail called, packetNo=" + packetNo);
        WalletModel.getInstance().getRedPacketDetail(packetNo, (code, msg, result) -> {
            android.util.Log.d("RedPacketOpen", "API response: code=" + code + ", msg=" + msg + ", result=" + (result != null ? "not null" : "null"));
            if (code == HttpResponseCode.success && result != null) {
                android.util.Log.d("RedPacketOpen", "RedPacketDetail: total_amount=" + result.total_amount + ", my_grab_amount=" + result.my_grab_amount);
                detail = result;
                updateUI();
            } else {
                android.util.Log.e("RedPacketOpen", "API failed: " + msg);
                WKToastUtils.getInstance().showToastFail(msg);
            }
        });
    }
    
    private void updateUI() {
        if (detail == null) return;
        
        // 检查是否已抢过
        String myUid = WKConfig.getInstance().getUid();
        boolean hasGrabbed = false;
        if (detail.records != null) {
            for (RedPacketDetail.RedPacketRecord record : detail.records) {
                if (myUid.equals(record.uid)) {
                    hasGrabbed = true;
                    break;
                }
            }
        }
        
        if (hasGrabbed || !detail.canGrab()) {
            // 已抢过或红包已结束，显示查看详情
            binding.openBtn.setVisibility(View.GONE);
            binding.detailBtn.setVisibility(View.VISIBLE);
        } else {
            // 可以抢
            binding.openBtn.setVisibility(View.VISIBLE);
            binding.detailBtn.setVisibility(View.GONE);
        }
    }
    
    private void grabRedPacket() {
        binding.openBtn.setEnabled(false);
        
        WalletModel.getInstance().grabRedPacket(packetNo, (code, msg) -> {
            binding.openBtn.setEnabled(true);
            
            if (code == HttpResponseCode.success) {
                // 抢成功，跳转详情
                Intent intent = new Intent(this, RedPacketDetailActivity.class);
                intent.putExtra("packet_no", packetNo);
                startActivity(intent);
                finish();
            } else {
                WKToastUtils.getInstance().showToastFail(msg);
            }
        });
    }
}

