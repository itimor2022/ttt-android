package com.chat.wallet.msgitem;

import android.os.Parcel;

import com.chat.base.msgitem.WKContentType;
import com.xinbida.wukongim.msgmodel.WKMessageContent;

import org.json.JSONObject;

/**
 * 红包消息内容
 */
public class RedPacketContent extends WKMessageContent {
    
    public String packetNo;
    public long amount;
    public String remark;
    public int status; // 0: 未领取, 1: 已领取, 2: 已抢完, 3: 已过期
    
    public RedPacketContent() {
        type = WKContentType.redPacket;
    }
    
    @Override
    public WKMessageContent decodeMsg(JSONObject jsonObject) {
        android.util.Log.d("RedPacketContent", "decodeMsg called, jsonObject=" + jsonObject.toString());
        packetNo = jsonObject.optString("packet_no");
        amount = jsonObject.optLong("amount");
        remark = jsonObject.optString("remark", "恭喜发财，大吉大利");
        status = jsonObject.optInt("status", 0);
        android.util.Log.d("RedPacketContent", "Decoded: packetNo=" + packetNo + ", amount=" + amount);
        return this;
    }
    
    @Override
    public JSONObject encodeMsg() {
        JSONObject json = new JSONObject();
        try {
            json.put("packet_no", packetNo);
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
        return "[红包] " + remark;
    }
    
    @Override
    public String getSearchableWord() {
        return "[红包]";
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(packetNo);
        dest.writeLong(amount);
        dest.writeString(remark);
        dest.writeInt(status);
    }
    
    protected RedPacketContent(Parcel in) {
        super(in);
        packetNo = in.readString();
        amount = in.readLong();
        remark = in.readString();
        status = in.readInt();
    }
    
    public static final Creator<RedPacketContent> CREATOR = new Creator<RedPacketContent>() {
        @Override
        public RedPacketContent createFromParcel(Parcel in) {
            return new RedPacketContent(in);
        }

        @Override
        public RedPacketContent[] newArray(int size) {
            return new RedPacketContent[size];
        }
    };
}
