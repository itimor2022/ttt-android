package com.chat.label.service

import com.chat.base.net.HttpResponseCode
import com.chat.label.entity.KLabel
import java.lang.ref.WeakReference

/**
 *
 * 2020-11-03 11:26
 * 标签
 */
class KLabelPresenter constructor(view: LabelContact.IKLabelView) : LabelContact.IKLabelPresenter {
    override fun deleteLabel(id: String) {
        KLabelModel().delete(id, object : KLabelModel.ILabelCommon {
            override fun onResult(code: Int, msg: String) {
                limlabelview.get()!!.hideLoading()
                if (code == HttpResponseCode.success.toInt())
                    limlabelview.get()!!.setDeleteLabelResult(id)
                else {
                    limlabelview.get()!!.showError(msg)
                }
            }
        })
    }

    override fun updateLabel(id: String, name: String, uids: List<String>) {
        KLabelModel().update(id, name, uids, object : KLabelModel.ILabelCommon {
            override fun onResult(code: Int, msg: String) {
                if (code == HttpResponseCode.success.toInt())
                    limlabelview.get()!!.setAddLabelResult()
                else {
                    limlabelview.get()!!.hideLoading()
                    limlabelview.get()!!.showError(msg)
                }
            }
        })
    }

    override fun addLabel(name: String, uids: List<String>) {
        KLabelModel().add(name, uids, object : KLabelModel.ILabelCommon {
            override fun onResult(code: Int, msg: String) {
                if (code == HttpResponseCode.success.toInt())
                    limlabelview.get()!!.setAddLabelResult()
                else {
                    limlabelview.get()!!.hideLoading()
                    limlabelview.get()!!.showError(msg)
                }
            }
        })
    }

    override fun getLabelDetail(id: String) {
        KLabelModel().detail(id, object : KLabelModel.ILabelDetail {
            override fun onResult(code: Int, msg: String, label: KLabel) {
                limlabelview.get()!!.hideLoading()
                if (code == HttpResponseCode.success.toInt())
                    limlabelview.get()!!.setLabelDetail(label)
                else {
                    limlabelview.get()!!.showError(msg)
                }
            }
        })
    }

    override fun getLabels() {
        KLabelModel().getLabels(object : KLabelModel.IGetLabels {
            override fun onResult(code: Int, msg: String, list: List<KLabel>) {
                limlabelview.get()!!.hideLoading()
                if (code == HttpResponseCode.success.toInt())
                    limlabelview.get()!!.setLabels(list)
                else {
                    limlabelview.get()!!.showError(msg)
                }
            }

        })
    }

    override fun showLoading() {}

    private var limlabelview: WeakReference<LabelContact.IKLabelView> = WeakReference(view)

}