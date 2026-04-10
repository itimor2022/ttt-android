package com.chat.wallet.ui;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivityWalletBinding;
import com.chat.wallet.entity.WalletInfo;
import com.chat.wallet.service.WalletModel;

/**
 * 钱包主页
 */
public class WalletActivity extends WKBaseActivity<ActivityWalletBinding> {

    private WalletInfo walletInfo;

    @Override
    protected ActivityWalletBinding getViewBinding() {
        return ActivityWalletBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.wallet_title);
    }

    @Override
    protected void initView() {
        // 设置沉浸式状态栏
        setupStatusBar();

        // 返回按钮
        wkVBinding.backIv.setOnClickListener(v -> finish());

        wkVBinding.rechargeLayout.setOnClickListener(v -> {
            if (checkRealNameVerify() && checkPayPassword()) {
                startActivity(new Intent(this, RechargeActivity.class));
            }
        });

        wkVBinding.withdrawLayout.setOnClickListener(v -> {
            if (checkRealNameVerify() && checkPayPassword()) {
                startActivity(new Intent(this, WithdrawActivity.class));
            }
        });

        wkVBinding.recordsLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, WalletRecordsActivity.class));
        });

        wkVBinding.recordsIv.setOnClickListener(v -> {
            startActivity(new Intent(this, WalletRecordsActivity.class));
        });

        wkVBinding.payPwdLayout.setOnClickListener(v -> {
            if (walletInfo != null && walletInfo.has_pay_pwd) {
                // 已设置密码，先弹窗验证原密码
                showVerifyOldPasswordDialog();
            } else {
                // 未设置密码，弹窗设置
                showSetPasswordDialog();
            }
        });

        // 实名认证
        wkVBinding.realNameLayout.setOnClickListener(v -> {
            if (walletInfo != null) {
                if (walletInfo.needRealNameVerify()) {
                    startActivity(new Intent(this, RealNameVerifyActivity.class));
                } else if (walletInfo.isRealNameAuditing()) {
                    showToast("实名认证正在审核中，请耐心等待");
                } else if (walletInfo.isRealNameVerified()) {
                    showToast("您已完成实名认证");
                }
            }
        });
    }

    private void setupStatusBar() {
        // 设置透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        // 设置状态栏占位高度
        ViewCompat.setOnApplyWindowInsetsListener(wkVBinding.statusBarView, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = statusBarHeight;
            v.setLayoutParams(params);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWalletInfo();
    }

    private void loadWalletInfo() {
        WalletModel.getInstance().getWalletInfo((code, msg, info) -> {
            if (code == HttpResponseCode.success && info != null) {
                walletInfo = info;
                updateUI();
            } else {
                showToast(msg);
            }
        });
    }

    private void updateUI() {
        if (walletInfo == null) return;

        wkVBinding.balanceTv.setText(walletInfo.getBalanceYuan());
        wkVBinding.frozenTv.setText(String.format("冻结金额: %s元", walletInfo.getFrozenYuan()));

        if (walletInfo.has_pay_pwd) {
            wkVBinding.payPwdStatusTv.setText("已设置");
        } else {
            wkVBinding.payPwdStatusTv.setText("未设置");
        }

        // 显示实名认证状态
        wkVBinding.realNameStatusTv.setText(walletInfo.getRealNameStatusText());

        // 显示利息信息
        wkVBinding.interestRateTv.setText(walletInfo.getInterestRateText());
        wkVBinding.todayInterestTv.setText(walletInfo.getTodayInterestYuan());
        wkVBinding.interestTv.setText(walletInfo.getInterestYuan());
    }

    private boolean checkPayPassword() {
        if (walletInfo == null) {
            showToast("加载钱包信息中...");
            return false;
        }
        if (!walletInfo.has_pay_pwd) {
            showToast("请先设置支付密码");
            showSetPasswordDialog();
            return false;
        }
        return true;
    }

    private boolean checkRealNameVerify() {
        if (walletInfo == null) {
            showToast("加载钱包信息中...");
            return false;
        }
        if (walletInfo.isRealNameAuditing()) {
            showToast("实名认证正在审核中，请耐心等待");
            return false;
        }
        if (walletInfo.needRealNameVerify()) {
            showToast("请先完成实名认证");
            startActivity(new Intent(this, RealNameVerifyActivity.class));
            return false;
        }
        return true;
    }

    private void showSetPasswordDialog() {
        SetPayPasswordDialog dialog = new SetPayPasswordDialog(this);
        dialog.setOnPasswordSetListener(() -> {
            // 密码设置成功，刷新钱包信息
            loadWalletInfo();
        });
        dialog.show();
    }

    private void showVerifyOldPasswordDialog() {
        PayPasswordDialog dialog = new PayPasswordDialog(this, password -> {
            // 验证原密码
            WalletModel.getInstance().verifyPayPassword(password, (code, msg) -> {
                if (code == HttpResponseCode.success) {
                    // 验证成功，进入修改密码页面
                    Intent intent = new Intent(this, SetPayPwdActivity.class);
                    intent.putExtra("is_modify", true);
                    intent.putExtra("old_password", password);
                    startActivity(intent);
                } else {
                    // 验证失败，重新弹窗
                    showToast(msg != null ? msg : "原密码错误");
                    showVerifyOldPasswordDialog();
                }
            });
        });
        dialog.setTitle("请输入原支付密码");
        dialog.show();
    }
}
