package com.chat.invite

import android.content.Intent
import com.chat.base.WKBaseApplication
import com.chat.base.config.WKConfig
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.PersonalInfoMenu
import com.chat.invite.ui.UserInviteCodeActivity

class WkInviteApplication {

    private object SingletonInstance {
        val INSTANCE = WkInviteApplication()
    }

    companion object {
        val instance: WkInviteApplication
            get() = SingletonInstance.INSTANCE
    }

    fun init() {
        EndpointManager.getInstance().setMethod(
            "user_invite_code", EndpointCategory.personalCenter, 99
        ) {
            PersonalInfoMenu(
                "invite_code",
                R.mipmap.ic_invite,
                WKBaseApplication.getInstance().context.getString(R.string.my_invite_code)
            ) {
                val intent = Intent(
                    WKBaseApplication.getInstance().context,
                    UserInviteCodeActivity::class.java
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                WKBaseApplication.getInstance().context.startActivity(intent)
            }
        }
    }
}