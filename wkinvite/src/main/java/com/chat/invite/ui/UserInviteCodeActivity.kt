package com.chat.invite.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.text.TextUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chat.base.base.WKBaseActivity
import com.chat.base.config.WKConfig
import com.chat.base.net.HttpResponseCode
import com.chat.base.ui.Theme
import com.chat.base.utils.WKDialogUtils
import com.chat.base.utils.WKToastUtils
import com.chat.base.utils.singleclick.SingleClickUtil
import com.chat.invite.R
import com.chat.invite.databinding.ActUserInviteCodeLayoutBinding
import com.chat.invite.entity.InviteCode
import com.chat.invite.service.InviteModel
import com.xinbida.wukongim.entity.WKChannelType

class UserInviteCodeActivity : WKBaseActivity<ActUserInviteCodeLayoutBinding>() {
    override fun getViewBinding(): ActUserInviteCodeLayoutBinding {
        return ActUserInviteCodeLayoutBinding.inflate(layoutInflater)
    }

    override fun setTitle(titleTv: TextView?) {
        titleTv?.setText(R.string.my_invite_code)
    }

    override fun initView() {
        wkVBinding.nameTv.text = WKConfig.getInstance().userName
        wkVBinding.avatarView.showAvatar(WKConfig.getInstance().uid, WKChannelType.PERSONAL)
        wkVBinding.resetTv.background = Theme.createSelectorDrawable(Theme.getPressedColor())
        wkVBinding.copyTv.background = Theme.createSelectorDrawable(Theme.getPressedColor())
    }

    override fun initListener() {
        SingleClickUtil.onSingleClick(wkVBinding.resetTv) {
            updateStatus()
        }

        wkVBinding.copyTv.setOnClickListener {
            val content = wkVBinding.codeTv.text.toString()
            if (TextUtils.isEmpty(content)) {
                WKDialogUtils.getInstance().showDialog(
                    this,
                    getString(R.string.str_base_tips),
                    getString(R.string.no_code_tip),
                    true,
                    getString(R.string.cancel),
                    getString(R.string.sure),
                    0,
                    0
                ) { index ->
                    if (index == 1) {
                        updateStatus()
                    }
                }
            } else {
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val mClipData = ClipData.newPlainText("Label", content)
                cm.setPrimaryClip(mClipData)
                showToast(getString(R.string.copyed))
            }

        }
    }

    override fun initData() {
        getInviteCode()
    }

    private fun getInviteCode() {
        InviteModel.instance.getInviteCode(object : InviteModel.IInviteCode {
            override fun onResult(code: Int, msg: String, inviteCode: InviteCode?) {
                if (code == HttpResponseCode.success.toInt()) {
                    wkVBinding.codeTv.text = inviteCode?.invite_code
                    if (inviteCode?.status == 1) {
                        wkVBinding.codeTv.setTextColor(
                            ContextCompat.getColor(
                                this@UserInviteCodeActivity,
                                R.color.colorDark
                            )
                        )
                        wkVBinding.resetTv.text = getString(R.string.disable_invite_code)
                    } else {
                        wkVBinding.codeTv.setTextColor(
                            ContextCompat.getColor(
                                this@UserInviteCodeActivity,
                                R.color.homeColor
                            )
                        )
                        wkVBinding.resetTv.text = getString(R.string.enable_invite_code)
                    }
                } else {
                    showToast(msg)
                }
            }
        })
    }

    private fun updateStatus() {
        InviteModel.instance.updateStatus { code, msg ->
            if (code == HttpResponseCode.success.toInt()) {
                getInviteCode()
            } else {
                showToast(msg)
            }
        }
    }
}