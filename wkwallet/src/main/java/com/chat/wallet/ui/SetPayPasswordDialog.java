package com.chat.wallet.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.chat.base.net.HttpResponseCode;
import com.chat.wallet.R;
import com.chat.wallet.service.WalletModel;

/**
 * 设置支付密码弹窗 - 仿微信风格
 */
public class SetPayPasswordDialog extends Dialog {
    
    private View rootView;
    private StringBuilder password = new StringBuilder();
    private StringBuilder confirmPassword = new StringBuilder();
    private boolean isConfirmStep = false;
    
    private View[] dotViews;
    private View[] boxViews;
    private FrameLayout step2Circle;
    private TextView step2Number;
    private TextView step1Text;
    private TextView step2Text;
    private View stepLine;
    private TextView hintTv;
    private TextView titleTv;
    private TextView errorTv;
    private View passwordLayout;
    
    private OnPasswordSetListener listener;
    
    public interface OnPasswordSetListener {
        void onSuccess();
    }
    
    public SetPayPasswordDialog(@NonNull Context context) {
        super(context, R.style.BottomSheetDialog);
    }
    
    public SetPayPasswordDialog setOnPasswordSetListener(OnPasswordSetListener listener) {
        this.listener = listener;
        return this;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_set_pay_password, null);
        setContentView(rootView);
        
        setupWindow();
        initViews();
        initKeyboard();
        updateStep();
    }
    
    private void setupWindow() {
        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setWindowAnimations(R.style.BottomDialogAnimation);
        }
    }
    
    private void initViews() {
        // 密码框
        dotViews = new View[]{
            rootView.findViewById(R.id.dot1),
            rootView.findViewById(R.id.dot2),
            rootView.findViewById(R.id.dot3),
            rootView.findViewById(R.id.dot4),
            rootView.findViewById(R.id.dot5),
            rootView.findViewById(R.id.dot6)
        };
        
        boxViews = new View[]{
            rootView.findViewById(R.id.box1),
            rootView.findViewById(R.id.box2),
            rootView.findViewById(R.id.box3),
            rootView.findViewById(R.id.box4),
            rootView.findViewById(R.id.box5),
            rootView.findViewById(R.id.box6)
        };
        
        // 步骤指示器
        step2Circle = rootView.findViewById(R.id.step2Circle);
        step2Number = rootView.findViewById(R.id.step2Number);
        step1Text = rootView.findViewById(R.id.step1Text);
        step2Text = rootView.findViewById(R.id.step2Text);
        stepLine = rootView.findViewById(R.id.stepLine);
        
        // 其他
        hintTv = rootView.findViewById(R.id.hintTv);
        titleTv = rootView.findViewById(R.id.titleTv);
        errorTv = rootView.findViewById(R.id.errorTv);
        passwordLayout = rootView.findViewById(R.id.passwordLayout);
        
        // 关闭按钮
        rootView.findViewById(R.id.closeBtn).setOnClickListener(v -> dismiss());
        
        // 初始化焦点
        updateFocusBox(0);
    }
    
    private void initKeyboard() {
        int[] keyIds = {R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4, 
                        R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9};
        
        for (int i = 0; i < keyIds.length; i++) {
            final int num = i;
            rootView.findViewById(keyIds[i]).setOnClickListener(v -> onKeyPress(String.valueOf(num)));
        }
        
        rootView.findViewById(R.id.keyDelete).setOnClickListener(v -> onDelete());
    }
    
    private void onKeyPress(String key) {
        errorTv.setVisibility(View.GONE);
        
        StringBuilder target = isConfirmStep ? confirmPassword : password;
        
        if (target.length() < 6) {
            target.append(key);
            updateDots(target.length());
            updateFocusBox(target.length());
            
            // 密码输入完成
            if (target.length() == 6) {
                if (!isConfirmStep) {
                    // 第一步完成，切换到确认密码
                    isConfirmStep = true;
                    resetPasswordInput();
                    updateStep();
                } else {
                    // 第二步完成，验证并提交
                    submitPassword();
                }
            }
        }
    }
    
    private void onDelete() {
        StringBuilder target = isConfirmStep ? confirmPassword : password;
        
        if (target.length() > 0) {
            target.deleteCharAt(target.length() - 1);
            updateDots(target.length());
            updateFocusBox(target.length());
        }
    }
    
    private void updateDots(int count) {
        for (int i = 0; i < dotViews.length; i++) {
            dotViews[i].setVisibility(i < count ? View.VISIBLE : View.INVISIBLE);
        }
    }
    
    private void updateFocusBox(int currentIndex) {
        for (int i = 0; i < boxViews.length; i++) {
            boxViews[i].setSelected(i == currentIndex && currentIndex < 6);
        }
    }
    
    private void resetPasswordInput() {
        updateDots(0);
        updateFocusBox(0);
    }
    
    private void updateStep() {
        if (isConfirmStep) {
            // 第二步
            hintTv.setText("请再次输入支付密码");
            step2Circle.setBackgroundResource(R.drawable.bg_step_active);
            step2Number.setTextColor(getContext().getResources().getColor(android.R.color.white, null));
            step2Text.setTextColor(getContext().getResources().getColor(R.color.colorAccent, null));
            stepLine.setBackgroundColor(getContext().getResources().getColor(R.color.colorAccent, null));
        } else {
            // 第一步
            hintTv.setText("请输入6位数字支付密码");
            step2Circle.setBackgroundResource(R.drawable.bg_step_inactive);
            step2Number.setTextColor(getContext().getResources().getColor(R.color.color999, null));
            step2Text.setTextColor(getContext().getResources().getColor(R.color.color999, null));
            stepLine.setBackgroundColor(getContext().getResources().getColor(R.color.colorLine, null));
        }
    }
    
    private void submitPassword() {
        String pwd = password.toString();
        String confirmPwd = confirmPassword.toString();
        
        if (!pwd.equals(confirmPwd)) {
            showError("两次输入的密码不一致");
            // 重置到第一步
            isConfirmStep = false;
            password.setLength(0);
            confirmPassword.setLength(0);
            resetPasswordInput();
            updateStep();
            shakePasswordLayout();
            return;
        }
        
        // 调用API设置密码
        WalletModel.getInstance().setPayPassword(pwd, (code, msg) -> {
            if (code == HttpResponseCode.success) {
                Toast.makeText(getContext(), "支付密码设置成功", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onSuccess();
                }
                dismiss();
            } else {
                showError(msg);
                // 重置
                isConfirmStep = false;
                password.setLength(0);
                confirmPassword.setLength(0);
                resetPasswordInput();
                updateStep();
            }
        });
    }
    
    private void showError(String message) {
        errorTv.setText(message);
        errorTv.setVisibility(View.VISIBLE);
    }
    
    private void shakePasswordLayout() {
        passwordLayout.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake));
    }
}

