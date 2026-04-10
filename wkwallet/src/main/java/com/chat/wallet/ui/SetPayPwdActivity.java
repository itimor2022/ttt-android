package com.chat.wallet.ui;

import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.databinding.ActivitySetPayPwdBinding;
import com.chat.wallet.service.WalletModel;

/**
 * 设置/修改支付密码页面
 * 修改密码时，原密码已在弹窗中验证通过
 */
public class SetPayPwdActivity extends WKBaseActivity<ActivitySetPayPwdBinding> {
    
    private boolean isModify = false;
    private String oldPassword = null; // 已验证的原密码
    
    // 输入模式: 0=新密码, 1=确认密码
    private int inputMode = 0;
    
    private StringBuilder password = new StringBuilder();
    private StringBuilder confirmPassword = new StringBuilder();
    
    private View[] pwdDots;
    private View[] confirmDots;
    private FrameLayout[] pwdBoxes;
    private FrameLayout[] confirmBoxes;
    
    @Override
    protected ActivitySetPayPwdBinding getViewBinding() {
        return ActivitySetPayPwdBinding.inflate(getLayoutInflater());
    }
    
    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(isModify ? R.string.wallet_change_pay_pwd : R.string.wallet_set_pay_pwd);
    }
    
    @Override
    protected void initView() {
        // 获取参数
        isModify = getIntent().getBooleanExtra("is_modify", false);
        oldPassword = getIntent().getStringExtra("old_password");
        
        // 修改密码时隐藏原密码区域（已在弹窗中验证）
        wkVBinding.oldPasswordCard.setVisibility(View.GONE);
        
        // 设置标题
        if (isModify) {
            wkVBinding.newPwdTitle.setText(R.string.wallet_input_new_pay_pwd);
        }
        
        // 初始化新密码圆点数组
        pwdDots = new View[] {
            wkVBinding.pwdDot1, wkVBinding.pwdDot2, wkVBinding.pwdDot3,
            wkVBinding.pwdDot4, wkVBinding.pwdDot5, wkVBinding.pwdDot6
        };
        
        pwdBoxes = new FrameLayout[] {
            wkVBinding.pwdBox1, wkVBinding.pwdBox2, wkVBinding.pwdBox3,
            wkVBinding.pwdBox4, wkVBinding.pwdBox5, wkVBinding.pwdBox6
        };
        
        confirmDots = new View[] {
            wkVBinding.confirmDot1, wkVBinding.confirmDot2, wkVBinding.confirmDot3,
            wkVBinding.confirmDot4, wkVBinding.confirmDot5, wkVBinding.confirmDot6
        };
        
        confirmBoxes = new FrameLayout[] {
            wkVBinding.confirmBox1, wkVBinding.confirmBox2, wkVBinding.confirmBox3,
            wkVBinding.confirmBox4, wkVBinding.confirmBox5, wkVBinding.confirmBox6
        };
        
        // 设置键盘点击事件
        setupKeyboard();
        
        // 点击切换输入模式
        wkVBinding.passwordInputLayout.setOnClickListener(v -> switchToMode(0));
        wkVBinding.confirmPasswordInputLayout.setOnClickListener(v -> switchToMode(1));
        
        // 默认选中第一个输入框
        inputMode = 0;
        updateFocusState();
    }
    
    private void setupKeyboard() {
        View[] keys = {
            wkVBinding.key0, wkVBinding.key1, wkVBinding.key2, wkVBinding.key3, wkVBinding.key4,
            wkVBinding.key5, wkVBinding.key6, wkVBinding.key7, wkVBinding.key8, wkVBinding.key9
        };
        
        for (int i = 0; i < keys.length; i++) {
            final int num = i;
            keys[i].setOnClickListener(v -> onNumberInput(String.valueOf(num)));
        }
        
        wkVBinding.keyDelete.setOnClickListener(v -> onDelete());
    }
    
    private void onNumberInput(String num) {
        wkVBinding.errorTv.setVisibility(View.GONE);
        
        StringBuilder target;
        View[] targetDots;
        
        if (inputMode == 0) {
            target = password;
            targetDots = pwdDots;
        } else {
            target = confirmPassword;
            targetDots = confirmDots;
        }
        
        if (target.length() < 6) {
            target.append(num);
            updateDots(targetDots, target.length());
            updateFocusState();
            
            // 输入完成后自动切换到下一步
            if (target.length() == 6) {
                if (inputMode == 0) {
                    // 新密码输入完成，切换到确认密码
                    switchToMode(1);
                } else {
                    // 确认密码输入完成，提交
                    submitPassword();
                }
            }
        }
    }
    
    private void onDelete() {
        StringBuilder target;
        View[] targetDots;
        
        if (inputMode == 0) {
            target = password;
            targetDots = pwdDots;
        } else {
            target = confirmPassword;
            targetDots = confirmDots;
        }
        
        if (target.length() > 0) {
            target.deleteCharAt(target.length() - 1);
            updateDots(targetDots, target.length());
            updateFocusState();
        }
    }
    
    private void updateDots(View[] targetDots, int count) {
        for (int i = 0; i < targetDots.length; i++) {
            targetDots[i].setVisibility(i < count ? View.VISIBLE : View.INVISIBLE);
        }
    }
    
    private void switchToMode(int mode) {
        // 不能跳过新密码
        if (mode == 1 && password.length() != 6) {
            showError("请先输入新支付密码");
            inputMode = 0;
            updateFocusState();
            scrollToCurrentInput();
            return;
        }
        
        inputMode = mode;
        updateFocusState();
        scrollToCurrentInput();
    }
    
    private void scrollToCurrentInput() {
        View targetView = inputMode == 0 ? wkVBinding.passwordInputLayout : wkVBinding.confirmPasswordInputLayout;
        wkVBinding.scrollView.post(() -> {
            wkVBinding.scrollView.smoothScrollTo(0, targetView.getTop());
        });
    }
    
    private void updateFocusState() {
        int pwdIndex = password.length();
        int confirmIndex = confirmPassword.length();
        
        // 更新新密码框焦点
        for (int i = 0; i < 6; i++) {
            pwdBoxes[i].setSelected(inputMode == 0 && i == pwdIndex && pwdIndex < 6);
        }
        
        // 更新确认密码框焦点
        for (int i = 0; i < 6; i++) {
            confirmBoxes[i].setSelected(inputMode == 1 && i == confirmIndex && confirmIndex < 6);
        }
    }
    
    private void submitPassword() {
        String pwd = password.toString();
        String confirmPwd = confirmPassword.toString();
        
        if (TextUtils.isEmpty(pwd) || pwd.length() != 6) {
            showError("请输入6位支付密码");
            switchToMode(0);
            return;
        }
        
        if (!pwd.equals(confirmPwd)) {
            showError("两次输入的密码不一致");
            // 清空确认密码并抖动
            confirmPassword.setLength(0);
            updateDots(confirmDots, 0);
            shakeView(wkVBinding.confirmPasswordInputLayout);
            inputMode = 1;
            updateFocusState();
            return;
        }
        
        loadingPopup.show();
        
        // 修改密码时传入已验证的原密码
        WalletModel.getInstance().setPayPassword(oldPassword, pwd, (code, msg) -> {
            loadingPopup.dismiss();
            if (code == HttpResponseCode.success) {
                showToast(isModify ? "密码修改成功" : "密码设置成功");
                finish();
            } else {
                showError(msg);
            }
        });
    }
    
    private void showError(String message) {
        wkVBinding.errorTv.setText(message);
        wkVBinding.errorTv.setVisibility(View.VISIBLE);
    }
    
    private void shakeView(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }
}
