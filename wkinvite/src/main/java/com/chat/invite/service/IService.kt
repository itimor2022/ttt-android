package com.chat.invite.service

import com.chat.base.net.entity.CommonResponse
import com.chat.invite.entity.InviteCode
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.PUT

interface IService {

    @GET("invite")
    fun getInvite(): Observable<InviteCode>

    @PUT("invite/reset")
    fun reset(): Observable<CommonResponse>

    @PUT("invite/status")
    fun updateStatus(): Observable<CommonResponse>
}