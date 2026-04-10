package com.chat.wallet.entity;

/**
 * 钱包信息
 */
public class WalletInfo {
    public long balance;           // 可用余额(分)
    public long frozen;            // 冻结金额(分)
    public long total_recharge;    // 累计充值(分)
    public long total_withdraw;    // 累计提现(分)
    public boolean has_pay_pwd; // 是否设置了支付密码
    public int real_name_status; // 实名认证状态：0-未认证，1-审核中，2-已认证，3-认证失败
    public String real_name; // 真实姓名
    public String id_card; // 身份证号
    public long interest; // 累计利息(分)
    public long today_interest; // 今日利息(分)
    public double interest_rate; // 年化利率(%)
    
    /**
     * 格式化余额(元)
     */
    public String getBalanceYuan() {
        return String.format("%.2f", balance / 100.0);
    }
    
    /**
     * 格式化冻结金额(元)
     */
    public String getFrozenYuan() {
        return String.format("%.2f", frozen / 100.0);
    }
    
    /**
     * 格式化累计利息(元)
     */
    public String getInterestYuan() {
        return String.format("%.2f", interest / 100.0);
    }
    
    /**
     * 格式化今日利息(元)
     */
    public String getTodayInterestYuan() {
        return String.format("%.2f", today_interest / 100.0);
    }
    
    /**
     * 获取年化利率文本
     */
    public String getInterestRateText() {
        return String.format("%.4f%%", interest_rate);
    }
    
    /**
     * 获取实名认证状态文本
     */
    public String getRealNameStatusText() {
        switch (real_name_status) {
            case 0:
                return "未认证";
            case 1:
                return "审核中";
            case 2:
                return "已认证";
            case 3:
                return "认证失败";
            default:
                return "未认证";
        }
    }
    
    /**
     * 是否已认证
     */
    public boolean isRealNameVerified() {
        return real_name_status == 2;
    }
    
    /**
     * 是否审核中
     */
    public boolean isRealNameAuditing() {
        return real_name_status == 1;
    }
    
    /**
     * 是否需要认证
     */
    public boolean needRealNameVerify() {
        return real_name_status == 0 || real_name_status == 3;
    }
}

