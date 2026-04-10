package com.chat.wallet.ui;

import android.os.Build;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKConfig;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivityRealNameVerifyBinding;
import com.chat.wallet.service.WalletModel;

/**
 * 实名认证
 */
public class RealNameVerifyActivity extends WKBaseActivity<ActivityRealNameVerifyBinding> {
    
    private CountDownTimer countDownTimer;
    
    @Override
    protected ActivityRealNameVerifyBinding getViewBinding() {
        return ActivityRealNameVerifyBinding.inflate(getLayoutInflater());
    }
    
    @Override
    protected void setTitle(TextView titleTv) {
        if (titleTv != null) {
            titleTv.setText("实名认证");
        }
    }
    
    @Override
    protected void initView() {
        // 设置沉浸式状态栏
        setupStatusBar();
        
        // 返回按钮
        wkVBinding.backIv.setOnClickListener(v -> finish());
        
        // 显示用户注册手机号（脱敏）
        UserInfoEntity userInfo = WKConfig.getInstance().getUserInfo();
        if (userInfo != null) {
            String phone = userInfo.phone;
            if (!TextUtils.isEmpty(phone)) {
                // 脱敏手机号：显示前3后4，中间用*代替
                String maskedPhone = maskPhoneNumber(phone);
                wkVBinding.phoneTv.setText(maskedPhone);
            }
        }
        
        // 获取验证码
        wkVBinding.getCodeTv.setOnClickListener(v -> {
            getVerificationCode();
        });
        
        // 提交认证
        wkVBinding.submitBtn.setOnClickListener(v -> {
            submitRealNameVerify();
        });
    }
    
    /**
     * 手机号脱敏处理
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    private String maskPhoneNumber(String phone) {
        if (TextUtils.isEmpty(phone) || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
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
    
    private void getVerificationCode() {
        // 从UserInfo中获取注册手机号
        UserInfoEntity userInfo = WKConfig.getInstance().getUserInfo();
        if (userInfo == null) {
            showToast("用户信息不存在");
            return;
        }
        
        String zone = userInfo.zone;
        String phone = userInfo.phone;
        
        // 验证手机号
        if (TextUtils.isEmpty(phone)) {
            showToast("注册手机号不存在");
            return;
        }
        
        if (TextUtils.isEmpty(zone)) {
            zone = "0086"; // 默认中国
        }
        
        // 调用API发送验证码（使用注册验证码接口）
        WalletModel.getInstance().sendVerificationCode(zone, phone, (code, msg) -> {
            if (code == HttpResponseCode.success) {
                startCountDown();
                showToast("验证码已发送到绑定手机号，请注意查收");
            } else {
                showToast(msg != null ? msg : "验证码发送失败");
            }
        });
    }
    
    private void startCountDown() {
        wkVBinding.getCodeTv.setEnabled(false);
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                wkVBinding.getCodeTv.setText(String.format("重新获取(%ds)", millisUntilFinished / 1000));
            }
            
            @Override
            public void onFinish() {
                wkVBinding.getCodeTv.setEnabled(true);
                wkVBinding.getCodeTv.setText("获取验证码");
            }
        };
        
        countDownTimer.start();
    }
    
    private void submitRealNameVerify() {
        String realName = wkVBinding.realNameEt.getText().toString().trim();
        String idCard = wkVBinding.idCardEt.getText().toString().trim();
        String code = wkVBinding.codeEt.getText().toString().trim();
        
        // 从UserInfo中获取注册手机号
        UserInfoEntity userInfo = WKConfig.getInstance().getUserInfo();
        if (userInfo == null) {
            showToast("用户信息不存在");
            return;
        }
        
        String phone = userInfo.phone;
        
        if (TextUtils.isEmpty(realName)) {
            showToast("请输入真实姓名");
            return;
        }
        
        if (TextUtils.isEmpty(idCard)) {
            showToast("请输入身份证号");
            return;
        }
        
        if (idCard.length() != 18) {
            showToast("请输入正确的身份证号");
            return;
        }
        
        if (TextUtils.isEmpty(phone)) {
            showToast("注册手机号不存在");
            return;
        }
        
        if (TextUtils.isEmpty(code)) {
            showToast("请输入验证码");
            return;
        }
        
        // 提交实名认证
        WalletModel.getInstance().submitRealNameVerify(realName, idCard, phone, code, (code1, msg) -> {
            if (code1 == HttpResponseCode.success) {
                showToast("提交成功，正在审核中");
                // 延迟关闭页面，让用户看到提示
                new android.os.Handler().postDelayed(() -> {
                    finish();
                }, 1000);
            } else {
                showToast(msg);
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}