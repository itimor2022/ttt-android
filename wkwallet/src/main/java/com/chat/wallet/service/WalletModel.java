package com.chat.wallet.service;

import com.alibaba.fastjson.JSONObject;
import com.chat.base.base.WKBaseModel;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.ICommonListener;
import com.chat.base.net.IRequestResultListener;
import com.chat.base.net.entity.CommonResponse;
import com.chat.login.entity.VerfiCodeResult;
import com.chat.wallet.entity.RedPacketDetail;
import com.chat.wallet.entity.TransferDetail;
import com.chat.wallet.entity.WalletBill;
import com.chat.wallet.entity.WalletInfo;
import com.chat.login.service.LoginService;
import com.chat.wallet.entity.WalletRecord;
import com.luck.picture.lib.utils.ToastUtils;

import java.util.List;

/**
 * 钱包业务模型
 */
public class WalletModel extends WKBaseModel {
    
    private WalletModel() {}
    
    private static class WalletModelBinder {
        private static final WalletModel model = new WalletModel();
    }
    
    public static WalletModel getInstance() {
        return WalletModelBinder.model;
    }
    
    // ========== 钱包 ==========

    // ========== 钱包 ==========
    public void getWalletInfo(IWalletInfoListener listener) {
        if (listener == null) return;

        request(createService(WalletService.class).getWalletInfo(), new IRequestResultListener<WalletInfo>() {
            @Override
            public void onSuccess(WalletInfo result) {
                android.util.Log.d("WalletDebug", "【onSuccess】 result = " + result);

                if (result != null) {
                    listener.onResult(HttpResponseCode.success, "请求成功", result);
                } else {
                    listener.onResult(HttpResponseCode.success, "服务器返回数据为空 (result = null)", null);
                }
            }

            @Override
            public void onFail(int code, String msg) {
                String errorMsg = "请求失败: code=" + code + ", msg=" + (msg != null ? msg : "未知错误");
                android.util.Log.d("WalletDebug", "【onFail】 " + errorMsg);
                listener.onResult(code, errorMsg, null);
            }
        });
    }
    
    public void setPayPassword(String password, ICommonListener listener) {
        setPayPassword(null, password, listener);
    }
    
    public void setPayPassword(String oldPassword, String password, ICommonListener listener) {
        JSONObject json = new JSONObject();
        if (oldPassword != null && !oldPassword.isEmpty()) {
            json.put("old_password", oldPassword);
        }
        json.put("password", password);
        request(createService(WalletService.class).setPayPassword(json), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                listener.onResult(HttpResponseCode.success, "");
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg);
            }
        });
    }
    
    public void verifyPayPassword(String password, ICommonListener listener) {
        JSONObject json = new JSONObject();
        json.put("password", password);
        request(createService(WalletService.class).verifyPayPassword(json), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                listener.onResult(HttpResponseCode.success, "");
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg);
            }
        });
    }

    // ========== 流水 ==========
    
    public void getRecords(int page, int pageSize, IRecordListListener listener) {
        request(createService(WalletService.class).getRecords(page, pageSize), new IRequestResultListener<List<WalletRecord>>() {
            @Override
            public void onSuccess(List<WalletRecord> result) {
                listener.onResult(HttpResponseCode.success, "", result);
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg, null);
            }
        });
    }
    
    public void getRecordsByType(int page, int pageSize, int type, IRecordListListener listener) {
        request(createService(WalletService.class).getRecordsByType(page, pageSize, type), new IRequestResultListener<List<WalletRecord>>() {
            @Override
            public void onSuccess(List<WalletRecord> result) {
                listener.onResult(HttpResponseCode.success, "", result);
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg, null);
            }
        });
    }
    
    public void getBills(int page, int pageSize, IBillListListener listener) {
        request(createService(WalletService.class).getBills(page, pageSize), new IRequestResultListener<List<WalletBill>>() {
            @Override
            public void onSuccess(List<WalletBill> result) {
                listener.onResult(HttpResponseCode.success, "", result);
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg, null);
            }
        });
    }
    
    // ========== 充值 ==========
    
    public void applyRecharge(long amount, String remark, ICommonListener listener) {
        JSONObject json = new JSONObject();
        json.put("amount", amount);
        json.put("remark", remark);
        request(createService(WalletService.class).applyRecharge(json), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                listener.onResult(HttpResponseCode.success, "");
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg);
            }
        });
    }
    
    // ========== 提现 ==========
    
    public void applyWithdraw(long amount, String realName, String bankName, String bankCard, 
                              String payPassword, String remark, ICommonListener listener) {
        JSONObject json = new JSONObject();
        json.put("amount", amount);
        json.put("real_name", realName);
        json.put("bank_name", bankName);
        json.put("bank_card", bankCard);
        json.put("pay_password", payPassword);
        json.put("remark", remark);
        request(createService(WalletService.class).applyWithdraw(json), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                listener.onResult(HttpResponseCode.success, "");
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg);
            }
        });
    }
    
    // ========== 转账 ==========
    
    public void sendTransfer(String toUid, long amount, String remark, String payPassword, ITransferListener listener) {
        JSONObject json = new JSONObject();
        json.put("to_uid", toUid);
        json.put("amount", amount);
        json.put("remark", remark);
        json.put("pay_password", payPassword);
        request(createService(WalletService.class).sendTransfer(json), new IRequestResultListener<TransferDetail>() {
            @Override
            public void onSuccess(TransferDetail result) {
                listener.onResult(HttpResponseCode.success, "", result);
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg, null);
            }
        });
    }
    
    public void receiveTransfer(String transferNo, ICommonListener listener) {
        request(createService(WalletService.class).receiveTransfer(transferNo), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                listener.onResult(HttpResponseCode.success, "");
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg);
            }
        });
    }
    
    public void refundTransfer(String transferNo, ICommonListener listener) {
        request(createService(WalletService.class).refundTransfer(transferNo), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                listener.onResult(HttpResponseCode.success, "");
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg);
            }
        });
    }
    
    public void getTransferDetail(String transferNo, ITransferListener listener) {
        request(createService(WalletService.class).getTransferDetail(transferNo), new IRequestResultListener<TransferDetail>() {
            @Override
            public void onSuccess(TransferDetail result) {
                listener.onResult(HttpResponseCode.success, "", result);
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg, null);
            }
        });
    }
    
    // ========== 红包 ==========
    
    public void sendRedPacket(String channelId, int channelType, int type, long totalAmount, 
                              int count, String remark, String payPassword, IRedPacketListener listener) {
        JSONObject json = new JSONObject();
        json.put("channel_id", channelId);
        json.put("channel_type", channelType);
        json.put("type", type);
        json.put("total_amount", totalAmount);
        json.put("total_count", count);
        json.put("remark", remark);
        json.put("pay_password", payPassword);
        request(createService(WalletService.class).sendRedPacket(json), new IRequestResultListener<RedPacketDetail>() {
            @Override
            public void onSuccess(RedPacketDetail result) {
                listener.onResult(HttpResponseCode.success, "", result);
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg, null);
            }
        });
    }
    
    public void grabRedPacket(String packetNo, ICommonListener listener) {
        request(createService(WalletService.class).grabRedPacket(packetNo), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                listener.onResult(HttpResponseCode.success, "");
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg);
            }
        });
    }
    
    public void getRedPacketDetail(String packetNo, IRedPacketListener listener) {
        request(createService(WalletService.class).getRedPacketDetail(packetNo), new IRequestResultListener<RedPacketDetail>() {
            @Override
            public void onSuccess(RedPacketDetail result) {
                listener.onResult(HttpResponseCode.success, "", result);
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg, null);
            }
        });
    }
    
    // ========== 实名认证 ==========
    
    public void submitRealNameVerify(String realName, String idCard, String phone, String code, ICommonListener listener) {
        JSONObject json = new JSONObject();
        json.put("real_name", realName);
        json.put("id_card", idCard);
        json.put("phone", phone);
        json.put("code", code);
        request(createService(WalletService.class).submitRealNameVerify(json), new IRequestResultListener<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse result) {
                listener.onResult(HttpResponseCode.success, "");
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg);
            }
        });
    }
    
    public void sendVerificationCode(String zone, String phone, ICommonListener listener) {
        JSONObject json = new JSONObject();
        json.put("zone", zone);
        json.put("phone", phone);
        request(createService(LoginService.class).registerCode(json), new IRequestResultListener<VerfiCodeResult>() {
            @Override
            public void onSuccess(VerfiCodeResult result) {
                listener.onResult(HttpResponseCode.success, "");
            }
            
            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg);
            }
        });
    }
    
    // ========== 回调接口 ==========
    
    public interface IWalletInfoListener {
        void onResult(int code, String msg, WalletInfo info);
    }
    
    public interface IRecordListListener {
        void onResult(int code, String msg, List<WalletRecord> list);
    }
    
    public interface IBillListListener {
        void onResult(int code, String msg, List<WalletBill> list);
    }
    
    public interface ITransferListener {
        void onResult(int code, String msg, TransferDetail detail);
    }
    
    public interface IRedPacketListener {
        void onResult(int code, String msg, RedPacketDetail detail);
    }
}

