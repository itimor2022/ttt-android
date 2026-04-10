package com.chat.wallet.entity;

import java.util.List;

/**
 * 红包详情
 */
public class RedPacketDetail {
    public long id;
    public String packet_no;       // 红包编号
    public String uid;             // 发送者UID
    public String channel_id;      // 频道ID
    public int channel_type;       // 频道类型
    public int type;               // 红包类型 1:普通红包 2:拼手气红包
    public long total_amount;      // 总金额(分)
    public int total_count;        // 总个数
    public long remain_amount;     // 剩余金额(分)
    public int remain_count;       // 剩余个数
    public String remark;          // 祝福语
    public int status;             // 状态 0:进行中 1:已抢完 2:已过期退回
    public String expire_time;     // 过期时间
    public String created_at;      // 创建时间
    public List<RedPacketRecord> records; // 领取记录
    public long my_grab_amount;    // 当前用户抢到的金额(分)
    public boolean my_grab_is_best; // 当前用户是否手气最佳
    
    /**
     * 格式化自己抢到的金额(元)
     */
    public String getMyGrabAmountYuan() {
        return String.format("%.2f", my_grab_amount / 100.0);
    }
    
    /**
     * 格式化总金额(元)
     */
    public String getTotalAmountYuan() {
        return String.format("%.2f", total_amount / 100.0);
    }
    
    /**
     * 获取类型名称
     */
    public String getTypeName() {
        return type == 2 ? "拼手气红包" : "普通红包";
    }
    
    /**
     * 获取状态名称
     */
    public String getStatusName() {
        switch (status) {
            case 0: return "进行中";
            case 1: return "已抢完";
            case 2: return "已过期退回";
            default: return "未知";
        }
    }
    
    /**
     * 是否可以抢
     */
    public boolean canGrab() {
        return status == 0 && remain_count > 0;
    }
    
    /**
     * 领取记录
     */
    public static class RedPacketRecord {
        public long id;
        public String uid;
        public String name;         // 用户昵称
        public String avatar;       // 用户头像
        public long amount;
        public int is_best;         // 是否手气最佳
        public String created_at;
        
        public String getAmountYuan() {
            return String.format("%.2f", amount / 100.0);
        }
        
        public boolean isBest() {
            return is_best == 1;
        }
        
        public String getDisplayName() {
            return name != null && !name.isEmpty() ? name : uid;
        }
    }
}

