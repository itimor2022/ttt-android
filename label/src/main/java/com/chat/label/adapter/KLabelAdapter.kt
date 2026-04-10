package com.chat.label.adapter

import android.view.View
import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.entity.PopupMenuItem
import com.chat.label.components.CustomerTouchListener
import com.chat.base.utils.WKDialogUtils
import com.chat.label.R
import com.chat.label.entity.KLabel

/**
 * 2020-11-03 11:47
 */

class KLabelAdapter : BaseQuickAdapter<KLabel, BaseViewHolder>(R.layout.item_label_layout) {
    override fun convert(holder: BaseViewHolder, item: KLabel) {
        holder.setText(R.id.nameTv, String.format("%s(%d)", item.name, item.count))
        //长按事件

        val list = ArrayList<PopupMenuItem>()
        list.add(
            PopupMenuItem(
                context.getString(R.string.k_delete),
                0,
                object : PopupMenuItem.IClick {
                    override fun onClick() {
                        iLongClick!!.onClick(item, 1)
                    }
                })
        )
        list.add(
            PopupMenuItem(
                context.getString(R.string.k_edit),
                0,
                object : PopupMenuItem.IClick {
                    override fun onClick() {
                        iLongClick!!.onClick(item, 2)
                    }
                })
        )
        holder.getView<LinearLayout>(R.id.contentLayout)
            .setOnTouchListener(CustomerTouchListener(
                object :
                    CustomerTouchListener.ICustomerTouchListener {
                    override fun onClick(view: View?, coordinate: FloatArray?) {
                    }

                    override fun onLongClick(view: View?, coordinate: FloatArray?) {
                        WKDialogUtils.getInstance().setViewLongClickPopup(view, list)
                    }

                    override fun onDoubleClick(view: View?, coordinate: FloatArray?) {
                    }

                }))
    }

    private var iLongClick: ILongClick? = null
    fun setILongClick(click: ILongClick) {
        iLongClick = click
    }

    interface ILongClick {
        fun onClick(label: KLabel, type: Int)
    }

}