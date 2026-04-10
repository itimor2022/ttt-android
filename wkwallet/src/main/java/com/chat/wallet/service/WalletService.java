package com.chat.wallet.service;

import com.alibaba.fastjson.JSONObject;
import com.chat.base.net.entity.CommonResponse;
import com.chat.wallet.entity.RedPacketDetail;
import com.chat.wallet.entity.TransferDetail;
import com.chat.wallet.entity.WalletBill;
import com.chat.wallet.entity.WalletInfo;
import com.chat.wallet.entity.WalletRecord;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 钱包服务接口
 */
public interface WalletService {
    
    // ========== 钱包 ==========
    
    @GET("wallet/balance")
    Observable<WalletInfo> getWalletInfo();
    
    @POST("wallet/pay_password")
    Observable<CommonResponse> setPayPassword(@Body JSONObject body);
    
    @POST("wallet/verify_pay_password")
    Observable<CommonResponse> verifyPayPassword(@Body JSONObject body);
    
    // ========== 流水 ==========
    
    @GET("wallet/records")
    Observable<List<WalletRecord>> getRecords(@Query("page") int page, @Query("page_size") int pageSize);
    
    @GET("wallet/records")
    Observable<List<WalletRecord>> getRecordsByType(@Query("page") int page, @Query("page_size") int pageSize, @Query("type") int type);
    
    // ========== 综合账单 ==========
    
    @GET("wallet/bills")
    Observable<List<WalletBill>> getBills(@Query("page") int page, @Query("page_size") int pageSize);
    
    // ========== 充值 ==========
    
    @POST("wallet/recharge")
    Observable<CommonResponse> applyRecharge(@Body JSONObject body);
    
    // ========== 提现 ==========
    
    @POST("wallet/withdraw")
    Observable<CommonResponse> applyWithdraw(@Body JSONObject body);
    
    // ========== 转账 ==========
    
    @POST("wallet/transfer/send")
    Observable<TransferDetail> sendTransfer(@Body JSONObject body);
    
    @POST("wallet/transfer/{transfer_no}/receive")
    Observable<CommonResponse> receiveTransfer(@Path("transfer_no") String transferNo);
    
    @POST("wallet/transfer/{transfer_no}/refund")
    Observable<CommonResponse> refundTransfer(@Path("transfer_no") String transferNo);
    
    @GET("wallet/transfer/{transfer_no}")
    Observable<TransferDetail> getTransferDetail(@Path("transfer_no") String transferNo);
    
    // ========== 红包 ==========
    
    @POST("wallet/redpacket/send")
    Observable<RedPacketDetail> sendRedPacket(@Body JSONObject body);
    
    @POST("wallet/redpacket/{packet_no}/grab")
    Observable<CommonResponse> grabRedPacket(@Path("packet_no") String packetNo);
    
    @GET("wallet/redpacket/{packet_no}")
    Observable<RedPacketDetail> getRedPacketDetail(@Path("packet_no") String packetNo);
    
    // ========== 实名认证 ==========
    
    @POST("wallet/real_name/verify")
    Observable<CommonResponse> submitRealNameVerify(@Body JSONObject body);
}

