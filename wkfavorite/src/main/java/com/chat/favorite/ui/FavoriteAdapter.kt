package com.chat.favorite.ui

import android.content.Intent
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.act.WKWebViewActivity
import com.chat.base.config.WKApiConfig
import com.chat.base.entity.PopupMenuItem
import com.chat.base.glide.GlideUtils
import com.chat.base.net.HttpResponseCode
import com.chat.base.ui.components.AvatarView
import com.chat.base.ui.components.NormalClickableContent
import com.chat.base.ui.components.NormalClickableSpan
import com.chat.base.utils.StringUtils
import com.chat.base.utils.WKDialogUtils
import com.chat.base.utils.WKToastUtils
import com.chat.favorite.R
import com.chat.favorite.WKFavoriteApplication
import com.chat.favorite.entity.FavoriteEntity
import com.chat.favorite.service.FavoriteModel
import com.xinbida.wukongim.entity.WKChannelType

class FavoriteAdapter(list: List<FavoriteEntity>) :
    BaseMultiItemQuickAdapter<FavoriteEntity, BaseViewHolder>() {
    init {
        addItemType(0, R.layout.item_no_data_layout)
        addItemType(1, R.layout.item_text_layout)
        addItemType(2, R.layout.item_img_layout)
        setList(list)
    }

    override fun convert(holder: BaseViewHolder, item: FavoriteEntity) {
        if (item.type == 1 || item.type == 2) {
            holder.setText(R.id.authTv, item.author_name)
            holder.setText(R.id.timeTv, item.created_at)
            val avatarView: AvatarView = holder.getView(R.id.avatarView)
            avatarView.setSize(20f)
            avatarView.showAvatar(
                item.author_uid,
                WKChannelType.PERSONAL,
                item.author_avatar
            )
            val content = item.payload?.get("content") as String
            when (item.type) {
                1 -> {
                    val contentTv: TextView = holder.getView(R.id.contentTv)
                    val spannableString = SpannableString(content)
                    //                    MoonUtil.identifyFaceExpression(getContext(), contentTv, content, MoonUtil.DEF_SCALE);
                    contentTv.movementMethod = LinkMovementMethod.getInstance()
                    val list = StringUtils.getStrUrls(content)
                    //                    List<Link> linkList = new ArrayList<>();
                    if (list.size > 0 && !TextUtils.isEmpty(content)) {
                        for (url in list) {
                            var fromIndex = 0
                            while (fromIndex >= 0) {
                                fromIndex = content.indexOf(url, fromIndex)
                                if (fromIndex >= 0) {
                                    spannableString.setSpan(
                                        StyleSpan(Typeface.BOLD), fromIndex,
                                        fromIndex + url.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                    spannableString.setSpan(
                                        NormalClickableSpan(true,
                                            ContextCompat.getColor(context, R.color.blue),
                                            NormalClickableContent(
                                                NormalClickableContent.NormalClickableTypes.Other,
                                                ""
                                            ),
                                            object : NormalClickableSpan.IClick {
                                                override fun onClick(view: View) {
                                                    val intent = Intent(
                                                        context,
                                                        WKWebViewActivity::class.java
                                                    )
                                                    intent.putExtra("url", url)
                                                    context.startActivity(intent)
                                                }
                                            }),
                                        fromIndex,
                                        fromIndex + url.length,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                    fromIndex += url.length
                                }
                            }
                        }
                    }
                    contentTv.text = spannableString
                    showDialog(
                        holder.getView(R.id.contentTv),
                        item.unique_key!!,
                        holder.bindingAdapterPosition
                    )
                    showDialog(
                        holder.getView(R.id.contentLayout),
                        item.unique_key!!,
                        holder.bindingAdapterPosition
                    )
                }
                2 -> {
                    showDialog(
                        holder.getView(R.id.contentLayout),
                        item.unique_key!!,
                        holder.bindingAdapterPosition
                    )
                    GlideUtils.getInstance().showImg(
                        context,
                        WKApiConfig.getShowUrl(content),
                        holder.getView(R.id.imageView)
                    )

                }
            }
        }
    }


    private fun showDialog(view: View, key: String, position: Int) {
        val list: MutableList<PopupMenuItem> = ArrayList()
        list.add(
            PopupMenuItem(context.getString(R.string.base_delete), R.mipmap.msg_delete,
                object : PopupMenuItem.IClick {
                    override fun onClick() {
                        FavoriteModel().delete(
                            key
                        ) { code: Int, msg: String? ->
                            if (code == HttpResponseCode.success.toInt()) {
                                removeAt(position)
                            } else WKToastUtils.getInstance().showToastNormal(msg)
                        }
                    }

                })
        )
        WKDialogUtils.getInstance().setViewLongClickPopup( view, list)
    }
}