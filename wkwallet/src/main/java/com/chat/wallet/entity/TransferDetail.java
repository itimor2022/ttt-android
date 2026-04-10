package com.chat.wallet.entity;

/**
 * 转账详情
 */
public class TransferDetail {
    public long id;
    public String transfer_no;     // 转账编号
    public String from_uid;        // 转出者UID
    public String to_uid;          // 接收者UID
    public long amount;            // 金额(分)
    public String remark;          // 备注
    public int status;             // 状态 0:待接收 1:已接收 2:已退回 3:已过期退回
    public String expire_time;     // 过期时间
    public String received_at;     // 接收/退回时间
    public String created_at;      // 创建时间
    
    /**
     * 格式化金额(元)
     */
    public String getAmountYuan() {
        return String.format("%.2f", amount / 100.0);
    }
    
    /**
     * 获取状态名称
     */
    public String getStatusName() {
        switch (status) {
            case 0: return "待领取";
            case 1: return "已领取";
            case 2: return "已退回";
            case 3: return "已过期退回";
            default: return "未知";
        }
    }
    
    /**
     * 是否可以领取
     */
    public boolean canReceive() {
        return status == 0;
    }
}

