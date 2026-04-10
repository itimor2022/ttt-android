package com.chat.customerservice

import android.content.Context
import android.text.TextUtils
import androidx.activity.ComponentActivity
import com.chat.base.WKBaseApplication
import com.chat.customerservice.entity.ChatInfo
import com.chat.customerservice.service.KCustomerServiceModel
import com.chat.base.base.WKBaseActivity
import com.chat.base.config.WKConfig
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.EndpointSID
import com.chat.base.endpoint.entity.ChatViewMenu
import com.chat.base.endpoint.entity.ContactsMenu
import com.chat.base.utils.WKToastUtils
import com.chat.wkcustomerservice.R

class WKCustomerServiceApplication private constructor() {
    private object SingletonInstance {
        val INSTANCE = WKCustomerServiceApplication()
    }

    companion object {
        val instance: WKCustomerServiceApplication
            get() = SingletonInstance.INSTANCE
    }

    fun init() {
        val appModule = WKBaseApplication.getInstance().getAppModuleWithSid("customerService")
        if (!WKBaseApplication.getInstance().appModuleIsInjection(appModule)) return

        addListener()
    }

    private fun addListener() {
        if (!TextUtils.isEmpty(WKConfig.getInstance().uid))
            KCustomerServiceModel().initVisitor()
        EndpointManager.getInstance().setMethod("", EndpointCategory.loginMenus) {
            KCustomerServiceModel().initVisitor()
            null
        }
        EndpointManager.getInstance().setMethod(
            "show_customer_service"
        ) { `object` ->
            val context = `object` as Context
            start(context)
            null
        }
        EndpointManager.getInstance().setMethod(
            EndpointCategory.mailList + "_customer_service",
            EndpointCategory.mailList,
            70
        ) { `object` ->
            val context = `object` as Context
            ContactsMenu(
                "customer_service",
                R.drawable.ic_customer_service,
                context.getString(R.string.customer_service)
            ) {
                start(context)
            }
        }

    }

    fun start(context: Context) {
        KCustomerServiceModel().getChatInfo(object :
            KCustomerServiceModel.IChatInfo {
            override fun onResult(code: Int, msg: String, chatInfo: ChatInfo?) {
                if (chatInfo != null) {
                    EndpointManager.getInstance().invoke(
                        EndpointSID.chatView,
                        ChatViewMenu(
                            context as ComponentActivity?,
                            chatInfo.channel_id,
                            chatInfo.channel_type,
                            0,
                            true
                        )
                    )
                } else {
                    WKToastUtils.getInstance().showToast(msg)
                }
            }
        })
    }
}