package com.chat.wallet.ui;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.base.WKBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivityWalletRecordsBinding;
import com.chat.wallet.entity.WalletBill;
import com.chat.wallet.service.WalletModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 钱包账单明细
 */
public class WalletRecordsActivity extends WKBaseActivity<ActivityWalletRecordsBinding> {
    
    private static final String TAG = "WalletRecords";
    private BillAdapter adapter;
    private int page = 1;
    private final int pageSize = 20;
    
    @Override
    protected ActivityWalletRecordsBinding getViewBinding() {
        return ActivityWalletRecordsBinding.inflate(getLayoutInflater());
    }
    
    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.wallet_records);
    }
    
    @Override
    protected void initView() {
        Log.d(TAG, "initView called");
        adapter = new BillAdapter();
        wkVBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        wkVBinding.recyclerView.setAdapter(adapter);
        
        wkVBinding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            Log.d(TAG, "onRefresh triggered");
            page = 1;
            loadBills();
        });
        
        wkVBinding.refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            page++;
            loadBills();
        });
        
        // 直接加载数据
        loadBills();
    }
    
    private void loadBills() {
        Log.d(TAG, "loadBills called, page=" + page + ", pageSize=" + pageSize);
        WalletModel.getInstance().getBills(page, pageSize, (code, msg, list) -> {
            Log.d(TAG, "getBills response: code=" + code + ", msg=" + msg + ", list size=" + (list != null ? list.size() : "null"));
            wkVBinding.refreshLayout.finishRefresh();
            wkVBinding.refreshLayout.finishLoadMore();
            
            if (code == HttpResponseCode.success) {
                if (page == 1) {
                    adapter.setList(list);
                } else {
                    if (list != null && !list.isEmpty()) {
                        adapter.addData(list);
                    }
                }
                
                if (list == null || list.size() < pageSize) {
                    wkVBinding.refreshLayout.finishLoadMoreWithNoMoreData();
                }
            } else {
                Log.e(TAG, "getBills failed: " + msg);
                showToast(msg);
            }
        });
    }
    
    private static class BillAdapter extends BaseQuickAdapter<WalletBill, BaseViewHolder> {
        
        public BillAdapter() {
            super(R.layout.item_wallet_bill, new ArrayList<>());
        }
        
        @Override
        protected void convert(BaseViewHolder holder, WalletBill item) {
            // 类型名称 + 状态
            String typeText = item.type_name;
            if (item.isPending()) {
                typeText += " (待审核)";
            } else if (item.isRejected()) {
                typeText += " (已拒绝)";
            }
            holder.setText(R.id.typeTv, typeText);
            
            // 备注
            String remarkText = item.remark != null && !item.remark.isEmpty() ? item.remark : "-";
            holder.setText(R.id.remarkTv, remarkText);
            
            // 时间
            holder.setText(R.id.timeTv, item.created_at);
            
            // 金额
            String amountText = item.isIncome() ? "+" + item.getAmountYuan() : "-" + item.getAmountYuan();
            holder.setText(R.id.amountTv, amountText);
            
            TextView amountTv = holder.getView(R.id.amountTv);
            TextView typeTv = holder.getView(R.id.typeTv);
            
            // 根据状态设置颜色
            if (item.isPending()) {
                amountTv.setTextColor(Color.parseColor("#FF9800")); // 橙色-待处理
                typeTv.setTextColor(Color.parseColor("#FF9800"));
            } else if (item.isRejected()) {
                amountTv.setTextColor(Color.parseColor("#9E9E9E")); // 灰色-已拒绝
                typeTv.setTextColor(Color.parseColor("#F44336")); // 红色提示
            } else {
                // 正常状态
                amountTv.setTextColor(getContext().getResources().getColor(
                    item.isIncome() ? R.color.green : R.color.red_packet_color
                ));
                typeTv.setTextColor(Color.parseColor("#333333"));
            }
        }
    }
}
