package com.chat.wallet.ui;

import android.text.TextUtils;
import android.widget.TextView;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivityWithdrawBinding;
import com.chat.wallet.service.WalletModel;

import java.math.BigDecimal;

/**
 * 提现页面
 */
public class WithdrawActivity extends WKBaseActivity<ActivityWithdrawBinding> {
    
    @Override
    protected ActivityWithdrawBinding getViewBinding() {
        return ActivityWithdrawBinding.inflate(getLayoutInflater());
    }
    
    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.withdraw);
    }
    
    @Override
    protected void initView() {
        wkVBinding.submitBtn.setOnClickListener(v -> submitWithdraw());
    }
    
    private void submitWithdraw() {
        String amountStr = wkVBinding.amountEt.getText().toString().trim();
        String realName = wkVBinding.realNameEt.getText().toString().trim();
        String bankName = wkVBinding.bankNameEt.getText().toString().trim();
        String bankCard = wkVBinding.bankCardEt.getText().toString().trim();
        String remark = wkVBinding.remarkEt.getText().toString().trim();
        
        if (TextUtils.isEmpty(amountStr)) {
            showToast("请输入提现金额");
            return;
        }
        if (TextUtils.isEmpty(realName)) {
            showToast("请输入真实姓名");
            return;
        }
        if (TextUtils.isEmpty(bankName)) {
            showToast("请输入银行名称");
            return;
        }
        if (TextUtils.isEmpty(bankCard)) {
            showToast("请输入银行卡号");
            return;
        }
        
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showToast("提现金额必须大于0");
                return;
            }
            
            long amountFen = amount.multiply(new BigDecimal(100)).longValue();
            
            // 弹出支付密码对话框
            PayPasswordDialog dialog = new PayPasswordDialog(this, password -> {
                loadingPopup.show();
                WalletModel.getInstance().applyWithdraw(amountFen, realName, bankName, bankCard, password, remark, (code, msg) -> {
                    loadingPopup.dismiss();
                    if (code == HttpResponseCode.success) {
                        showToast("提现申请已提交，请等待审核");
                        finish();
                    } else {
                        showToast(msg);
                    }
                });
            });
            dialog.setTitle("提现");
            dialog.setAmount("¥" + amountStr);
            dialog.show();
        } catch (NumberFormatException e) {
            showToast("请输入有效金额");
        }
    }
}

