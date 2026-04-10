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

import com.chat.wallet.R;
import com.chat.wallet.databinding.DialogPayPasswordBinding;

/**
 * 支付密码对话框 - 底部弹出式安全键盘
 */
public class PayPasswordDialog extends Dialog {
    
    private DialogPayPasswordBinding binding;
    private OnPasswordConfirmListener listener;
    private StringBuilder password = new StringBuilder();
    private View[] dotViews;
    private View[] boxViews;
    private String title;
    private String amount;
    
    public interface OnPasswordConfirmListener {
        void onConfirm(String password);
    }
    
    public PayPasswordDialog(@NonNull Context context, OnPasswordConfirmListener listener) {
        super(context, R.style.BottomSheetDialog);
        this.listener = listener;
    }
    
    public PayPasswordDialog setTitle(String title) {
        this.title = title;
        return this;
    }
    
    public PayPasswordDialog setAmount(String amount) {
        this.amount = amount;
        return this;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogPayPasswordBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        
        setupWindow();
        initViews();
        initKeyboard();
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
        // 初始化密码点视图
        View passwordInput = binding.passwordInput.getRoot();
        dotViews = new View[]{
            passwordInput.findViewById(R.id.dot1),
            passwordInput.findViewById(R.id.dot2),
            passwordInput.findViewById(R.id.dot3),
            passwordInput.findViewById(R.id.dot4),
            passwordInput.findViewById(R.id.dot5),
            passwordInput.findViewById(R.id.dot6)
        };
        
        boxViews = new View[]{
            passwordInput.findViewById(R.id.box1),
            passwordInput.findViewById(R.id.box2),
            passwordInput.findViewById(R.id.box3),
            passwordInput.findViewById(R.id.box4),
            passwordInput.findViewById(R.id.box5),
            passwordInput.findViewById(R.id.box6)
        };
        
        // 设置标题和金额
        if (title != null) {
            binding.titleTv.setText(title);
        }
        if (amount != null) {
            binding.amountTv.setText(amount);
            binding.amountLayout.setVisibility(View.VISIBLE);
        }
        
        binding.closeBtn.setOnClickListener(v -> dismiss());
        binding.forgetPwdTv.setOnClickListener(v -> {
            Toast.makeText(getContext(), "请联系客服重置支付密码", Toast.LENGTH_SHORT).show();
        });
        
        updateFocusBox();
    }
    
    private void initKeyboard() {
        View keyboard = binding.securityKeyboard.getRoot();
        
        // 数字键
        int[] keyIds = {R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4, 
                        R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9};
        
        for (int i = 0; i < keyIds.length; i++) {
            final int num = i;
            keyboard.findViewById(keyIds[i]).setOnClickListener(v -> onKeyPress(String.valueOf(num)));
        }
        
        // 删除键
        keyboard.findViewById(R.id.keyDelete).setOnClickListener(v -> onDelete());
    }
    
    private void onKeyPress(String key) {
        if (password.length() < 6) {
            password.append(key);
            updateDots();
            updateFocusBox();
            
            // 密码输入完成
            if (password.length() == 6) {
                onPasswordComplete();
            }
        }
    }
    
    private void onDelete() {
        if (password.length() > 0) {
            password.deleteCharAt(password.length() - 1);
            updateDots();
            updateFocusBox();
        }
    }
    
    private void updateDots() {
        for (int i = 0; i < dotViews.length; i++) {
            dotViews[i].setVisibility(i < password.length() ? View.VISIBLE : View.INVISIBLE);
        }
    }
    
    private void updateFocusBox() {
        for (int i = 0; i < boxViews.length; i++) {
            if (i == password.length()) {
                boxViews[i].setBackgroundResource(R.drawable.bg_password_box_focused);
            } else {
                boxViews[i].setBackgroundResource(R.drawable.bg_password_box);
            }
        }
    }
    
    private void onPasswordComplete() {
        if (listener != null) {
            listener.onConfirm(password.toString());
        }
        dismiss();
    }
    
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        // 清空密码
        password = new StringBuilder();
        updateDots();
        updateFocusBox();
        // 震动效果
        View passwordInput = binding.passwordInput.getRoot();
        passwordInput.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake));
    }
    
    public void clearPassword() {
        password = new StringBuilder();
        updateDots();
        updateFocusBox();
    }
}
