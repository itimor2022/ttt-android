package com.chat.invite.service

import com.chat.base.base.WKBaseModel
import com.chat.base.net.HttpResponseCode
import com.chat.base.net.ICommonListener
import com.chat.base.net.IRequestResultListener
import com.chat.base.net.entity.CommonResponse
import com.chat.invite.entity.InviteCode

class InviteModel private constructor() : WKBaseModel() {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = InviteModel()
    }

    interface IInviteCode {
        fun onResult(code: Int, msg: String, inviteCode: InviteCode?)
    }

    fun getInviteCode(iInviteCode: IInviteCode) {
        request(createService(IService::class.java).getInvite(),
            object : IRequestResultListener<InviteCode> {
                override fun onSuccess(result: InviteCode) {
                    iInviteCode.onResult(HttpResponseCode.success.toInt(), "", result)
                }

                override fun onFail(code: Int, msg: String) {
                    iInviteCode.onResult(code, msg, null)
                }
            })
    }

    fun reset(iCommonListener: ICommonListener) {
        request(createService(IService::class.java).reset(),
            object : IRequestResultListener<CommonResponse> {
                override fun onSuccess(result: CommonResponse) {
                    iCommonListener.onResult(HttpResponseCode.success.toInt(), "")
                }

                override fun onFail(code: Int, msg: String) {
                    iCommonListener.onResult(code, msg)
                }
            })
    }

    fun updateStatus(iCommonListener: ICommonListener) {
        request(createService(IService::class.java).updateStatus(),
            object : IRequestResultListener<CommonResponse> {
                override fun onSuccess(result: CommonResponse) {
                    iCommonListener.onResult(HttpResponseCode.success.toInt(), "")
                }

                override fun onFail(code: Int, msg: String) {
                    iCommonListener.onResult(code, msg)
                }
            })
    }
}