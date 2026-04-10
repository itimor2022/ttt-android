package com.chat.wallet.ui;

import android.text.TextUtils;
import android.widget.TextView;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivityRechargeBinding;
import com.chat.wallet.service.WalletModel;

import java.math.BigDecimal;

/**
 * 充值页面
 */
public class RechargeActivity extends WKBaseActivity<ActivityRechargeBinding> {
    
    @Override
    protected ActivityRechargeBinding getViewBinding() {
        return ActivityRechargeBinding.inflate(getLayoutInflater());
    }
    
    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.recharge);
    }
    
    @Override
    protected void initView() {
        wkVBinding.submitBtn.setOnClickListener(v -> submitRecharge());
    }
    
    private void submitRecharge() {
        String amountStr = wkVBinding.amountEt.getText().toString().trim();
        String remark = wkVBinding.remarkEt.getText().toString().trim();
        
        if (TextUtils.isEmpty(amountStr)) {
            showToast("请输入充值金额");
            return;
        }
        
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showToast("充值金额必须大于0");
                return;
            }
            
            long amountFen = amount.multiply(new BigDecimal(100)).longValue();
            
            loadingPopup.show();
            WalletModel.getInstance().applyRecharge(amountFen, remark, (code, msg) -> {
                loadingPopup.dismiss();
                if (code == HttpResponseCode.success) {
                    showToast("充值申请已提交，请等待审核");
                    finish();
                } else {
                    showToast(msg);
                }
            });
        } catch (NumberFormatException e) {
            showToast("请输入有效金额");
        }
    }
}

