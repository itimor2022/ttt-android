package com.chat.label

import android.content.Context
import android.content.Intent
import com.xinbida.wukongim.entity.WKChannel
import com.xinbida.wukongim.entity.WKChannelType
import com.chat.base.WKBaseApplication
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.ChooseLabelEntity
import com.chat.base.endpoint.entity.ChooseLabelMenu
import com.chat.base.endpoint.entity.ChooseContactsMenu
import com.chat.base.endpoint.entity.ContactsMenu
import com.chat.label.activitys.LabelActivity
import com.chat.label.entity.KLabel
import com.chat.label.service.KLabelModel

/**
 * 2020-11-02 17:00
 * 标签
 */
class WKLabelApplication private constructor() {
    private object SingletonInstance {
        val INSTANCE = WKLabelApplication()
    }

    companion object {
        val instance: WKLabelApplication
            get() = SingletonInstance.INSTANCE
    }

    fun init(context: Context) {
        val appModule = WKBaseApplication.getInstance().getAppModuleWithSid("label")
        if (!WKBaseApplication.getInstance().appModuleIsInjection(appModule)) return

        addListener(context)
    }

    private fun addListener(mContext: Context) {
        EndpointManager.getInstance().setMethod(
            EndpointCategory.mailList + "_label",
            EndpointCategory.mailList,
            80
        ) {
            ContactsMenu(
                "label",
                R.drawable.icon_label,
                mContext.getString(R.string.k_label)
            ) {
                val intent = Intent(mContext, LabelActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mContext.startActivity(intent)

            }
        }
        EndpointManager.getInstance().setMethod("k_choose_label") { `object` ->
            kChooseLabelMenu = `object` as ChooseLabelMenu
            KLabelModel().getLabels(object : KLabelModel.IGetLabels {
                override fun onResult(code: Int, msg: String, list: List<KLabel>) {
                    val labels = ArrayList<ChooseLabelEntity>()
                    for (label in list) run {
                        val entity =
                            ChooseLabelEntity()
                        entity.labelId = label.id
                        entity.labelName = label.name
                        val members = ArrayList<WKChannel>()
                        for (member in label.members!!) {
                            val channel = WKChannel()
                            channel.channelID = member.uid
                            channel.channelType = WKChannelType.PERSONAL
                            channel.channelName = member.name
                            channel.channelRemark = member.remark
                            members.add(channel)
                        }
                        entity.members = members
                        labels.add(entity)
                    }
                    kChooseLabelMenu!!.iChooseLabel.onResult(labels)
                }

            })

            null
        }
    }

    private var kChooseLabelMenu: ChooseLabelMenu? = null

}




