package com.chat.wallet.entity;

/**
 * 钱包流水记录
 */
public class WalletRecord {
    public long id;
    public String record_no;       // 流水号
    public int type;               // 类型 1:充值 2:提现 3:转账收入 4:转账支出 5:红包发出 6:红包收入 7:红包退回
    public long amount;            // 金额(分)
    public long balance_before;    // 变动前余额(分)
    public long balance_after;     // 变动后余额(分)
    public String remark;          // 备注
    public String related_id;      // 关联ID
    public String related_uid;     // 关联用户UID
    public String created_at;      // 创建时间
    
    /**
     * 格式化金额(元)
     */
    public String getAmountYuan() {
        return String.format("%.2f", amount / 100.0);
    }
    
    /**
     * 获取类型名称
     */
    public String getTypeName() {
        switch (type) {
            case 1: return "充值";
            case 2: return "提现";
            case 3: return "转账收入";
            case 4: return "转账支出";
            case 5: return "发红包";
            case 6: return "收红包";
            case 7: return "红包退回";
            default: return "其他";
        }
    }
    
    /**
     * 是否是收入
     */
    public boolean isIncome() {
        return type == 1 || type == 3 || type == 6 || type == 7;
    }
}

