package com.chat.wallet.ui;

import android.text.TextUtils;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivityRedPacketDetailBinding;
import com.chat.wallet.entity.RedPacketDetail;
import com.chat.wallet.service.WalletModel;

import java.util.ArrayList;

/**
 * 红包详情页面
 */
public class RedPacketDetailActivity extends WKBaseActivity<ActivityRedPacketDetailBinding> {
    
    private String packetNo;
    private RecordAdapter adapter;
    
    @Override
    protected ActivityRedPacketDetailBinding getViewBinding() {
        return ActivityRedPacketDetailBinding.inflate(getLayoutInflater());
    }
    
    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.red_packet_detail);
    }
    
    @Override
    protected void initView() {
        // 先获取参数（在 initData() 之前执行）
        packetNo = getIntent().getStringExtra("packet_no");
        android.util.Log.d("RedPacketDetail", "initView: packetNo=" + packetNo);
        
        if (TextUtils.isEmpty(packetNo)) {
            android.util.Log.e("RedPacketDetail", "packetNo is empty, finishing activity");
            showToast("红包不存在");
            finish();
            return;
        }
        
        adapter = new RecordAdapter();
        wkVBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        wkVBinding.recyclerView.setAdapter(adapter);
        
        loadDetail();
    }
    
    @Override
    protected void initData() {
        // 参数已在 initView() 中获取
    }
    
    private void loadDetail() {
        android.util.Log.d("RedPacketDetail", "loadDetail called, packetNo=" + packetNo);
        WalletModel.getInstance().getRedPacketDetail(packetNo, (code, msg, detail) -> {
            android.util.Log.d("RedPacketDetail", "API response: code=" + code + ", msg=" + msg + ", detail=" + (detail != null ? "not null" : "null"));
            if (code == HttpResponseCode.success && detail != null) {
                updateUI(detail);
            } else {
                android.util.Log.e("RedPacketDetail", "API failed: " + msg);
                showToast(msg);
            }
        });
    }
    
    private void updateUI(RedPacketDetail detail) {
        // 调试日志
        android.util.Log.d("RedPacketDetail", "RedPacketDetail: packet_no=" + detail.packet_no +
            ", total_amount=" + detail.total_amount + ", my_grab_amount=" + detail.my_grab_amount +
            ", remark=" + detail.remark + ", status=" + detail.status);
        
        wkVBinding.remarkTv.setText(detail.remark);
        
        // 如果自己抢到了红包，显示自己抢到的金额；否则显示红包总金额
        if (detail.my_grab_amount > 0) {
            android.util.Log.d("RedPacketDetail", "显示抢到的金额: " + detail.getMyGrabAmountYuan());
            wkVBinding.amountTv.setText(detail.getMyGrabAmountYuan());
        } else {
            android.util.Log.d("RedPacketDetail", "显示总金额: " + detail.getTotalAmountYuan());
            wkVBinding.amountTv.setText(detail.getTotalAmountYuan());
        }
        
        int receivedCount = detail.total_count - detail.remain_count;
        String statusText = String.format("已领取 %d/%d 个", receivedCount, detail.total_count);
        if (detail.my_grab_is_best) {
            statusText += " · 手气最佳";
        }
        wkVBinding.statusTv.setText(statusText);
        
        if (detail.records != null && !detail.records.isEmpty()) {
            adapter.setList(detail.records);
        }
    }
    
    private static class RecordAdapter extends BaseQuickAdapter<RedPacketDetail.RedPacketRecord, BaseViewHolder> {
        
        public RecordAdapter() {
            super(R.layout.item_red_packet_record, new ArrayList<>());
        }
        
        @Override
        protected void convert(BaseViewHolder holder, RedPacketDetail.RedPacketRecord item) {
            // 显示昵称
            holder.setText(R.id.uidTv, item.getDisplayName());
            holder.setText(R.id.amountTv, item.getAmountYuan() + "元");
            holder.setText(R.id.timeTv, item.created_at);
            holder.setVisible(R.id.bestTv, item.isBest());
            
            // 显示头像
            android.widget.ImageView avatarIv = holder.getView(R.id.avatarIv);
            if (avatarIv != null) {
                // 使用 channelID + channelType 的方式加载头像
                com.chat.base.glide.GlideUtils.getInstance().showAvatarImg(
                    getContext(), 
                    item.uid, 
                    com.xinbida.wukongim.entity.WKChannelType.PERSONAL,
                    item.uid,
                    avatarIv
                );
            }
        }
    }
}

