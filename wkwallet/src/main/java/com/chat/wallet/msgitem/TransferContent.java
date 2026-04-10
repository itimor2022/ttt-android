package com.chat.wallet.msgitem;

import android.os.Parcel;

import com.chat.base.msgitem.WKContentType;
import com.xinbida.wukongim.msgmodel.WKMessageContent;

import org.json.JSONObject;

/**
 * 转账消息内容
 */
public class TransferContent extends WKMessageContent {
    
    public String transferNo;
    public long amount;
    public String remark;
    public int status; // 0:待接收 1:已接收 2:已退回 3:已过期退回
    
    public TransferContent() {
        type = WKContentType.transfer;
    }
    
    @Override
    public WKMessageContent decodeMsg(JSONObject jsonObject) {
        android.util.Log.d("TransferContent", "decodeMsg called, jsonObject=" + jsonObject.toString());
        transferNo = jsonObject.optString("transfer_no");
        amount = jsonObject.optLong("amount");
        remark = jsonObject.optString("remark", "");
        status = jsonObject.optInt("status", 0);
        android.util.Log.d("TransferContent", "Decoded: transferNo=" + transferNo + ", amount=" + amount);
        return this;
    }
    
    @Override
    public JSONObject encodeMsg() {
        JSONObject json = new JSONObject();
        try {
            json.put("transfer_no", transferNo);
            json.put("amount", amount);
            json.put("remark", remark);
            json.put("status", status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
    
    @Override
    public String getDisplayContent() {
        return "[转账] " + (remark.isEmpty() ? "转账" : remark);
    }
    
    @Override
    public String getSearchableWord() {
        return "[转账]";
    }
    
    public String getStatusText() {
        switch (status) {
            case 0:
                return "待领取";
            case 1:
                return "已领取";
            case 2:
                return "已退回";
            case 3:
                return "已过期退回";
            default:
                return "";
        }
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(transferNo);
        dest.writeLong(amount);
        dest.writeString(remark);
        dest.writeInt(status);
    }
    
    protected TransferContent(Parcel in) {
        super(in);
        transferNo = in.readString();
        amount = in.readLong();
        remark = in.readString();
        status = in.readInt();
    }
    
    public static final Creator<TransferContent> CREATOR = new Creator<TransferContent>() {
        @Override
        public TransferContent createFromParcel(Parcel in) {
            return new TransferContent(in);
        }

        @Override
        public TransferContent[] newArray(int size) {
            return new TransferContent[size];
        }
    };
}
