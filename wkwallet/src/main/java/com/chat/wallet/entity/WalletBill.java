package com.chat.wallet.entity;

/**
 * 综合账单记录（包含充值/提现申请）
 */
public class WalletBill {
    public long id;
    public String bill_no;          // 账单号
    public String bill_type;        // 类型: record/recharge/withdraw
    public int type;                // 类型代码
    public String type_name;        // 类型名称
    public long amount;             // 金额(分)
    public int status;              // 状态 0-待处理 1-成功 2-已通过 3-已拒绝
    public String status_name;      // 状态名称
    public String remark;           // 备注
    public String created_at;       // 创建时间
    
    /**
     * 格式化金额(元)
     */
    public String getAmountYuan() {
        return String.format("%.2f", amount / 100.0);
    }
    
    /**
     * 是否是收入类型
     */
    public boolean isIncome() {
        // 充值(1)、转账收入(3)、红包收入(6)、红包/转账退回(7,8)是收入
        return type == 1 || type == 3 || type == 6 || type == 7 || type == 8;
    }
    
    /**
     * 是否待处理
     * 状态: 0-待审核 1-已通过 2-已拒绝
     */
    public boolean isPending() {
        return status == 0;
    }
    
    /**
     * 是否被拒绝
     * 状态: 0-待审核 1-已通过 2-已拒绝
     */
    public boolean isRejected() {
        return status == 2;
    }
}

