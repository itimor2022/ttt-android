package com.chat.label.activitys

import android.content.Intent
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.chat.base.base.WKBaseActivity
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.ChooseContactsMenu
import com.chat.base.utils.singleclick.SingleClickUtil
import com.chat.label.R
import com.chat.label.adapter.KLabelAdapter
import com.chat.label.databinding.ActLabelLayoutBinding
import com.chat.label.entity.KLabel
import com.chat.label.service.LabelContact
import com.chat.label.service.KLabelPresenter

/**
 * 2020-11-02 18:12
 * 标签列表
 */
class LabelActivity : WKBaseActivity<ActLabelLayoutBinding>(),
    LabelContact.IKLabelView {
    private lateinit var adapter: KLabelAdapter
    lateinit var presenter: KLabelPresenter

    override fun setTitle(titleTv: TextView) {
        titleTv.setText(R.string.k_label)
    }

    override fun getRightTvText(textView: TextView?): String {
        return getString(R.string.k_label_create)

    }


    override fun rightLayoutClick() {
        super.rightLayoutClick()
        EndpointManager.getInstance().invoke("choose_contacts",
            ChooseContactsMenu(
                Int.MAX_VALUE,  // maxChooseCount
                false,         // showGroup
                false,         // showBot
                null
            ) { selectedList ->
                val intent = Intent(this@LabelActivity, LabelDetailActivity::class.java)
                intent.putParcelableArrayListExtra(
                    "list",
                    selectedList as java.util.ArrayList<out Parcelable>
                )
                chooseResultLac.launch(intent)
            }
        )

    }

    override fun initPresenter() {
        presenter = KLabelPresenter(this)
    }

    override fun initView() {
        presenter.getLabels()
        val list: ArrayList<KLabel> = arrayListOf()
        adapter = KLabelAdapter()
        initAdapter(wkVBinding.recyclerView, adapter)
        adapter.setList(list)
    }

    override fun initListener() {
        adapter.addChildClickViewIds(R.id.contentLayout)
        adapter.setOnItemChildClickListener { _, view, position ->
            SingleClickUtil.determineTriggerSingleClick(view) {
                val label: KLabel = adapter.getItem(position)
                val intent = Intent(this, LabelDetailActivity::class.java)
                intent.putExtra("id", label.id)
                intent.putExtra("name", label.name)
                chooseResultLac.launch(intent)
            }
        }
        adapter.setILongClick(object : KLabelAdapter.ILongClick {
            override fun onClick(label: KLabel, type: Int) {
                if (type == 1) {
                    showDialog(getString(R.string.k_label_delete_tips), fun(it: Int) {
                        if (it == 1) {
                            loadingPopup.show()
                            presenter.deleteLabel(label.id)
                        }
                    })
                } else {
                    val intent = Intent(this@LabelActivity, LabelDetailActivity::class.java)
                    intent.putExtra("id", label.id)
                    intent.putExtra("name", label.name)
                    chooseResultLac.launch(intent)
                }
            }
        })
    }

    override fun setLabels(list: List<KLabel>) {
        if (list.isNotEmpty()) {
            adapter.setList(list)
            wkVBinding.noDataView.visibility = View.GONE
        } else {
            wkVBinding.noDataView.visibility = View.VISIBLE
        }
    }

    override fun showError(msg: String?) {
    }

    override fun hideLoading() {
        loadingPopup.dismiss()
    }

    override fun setLabelDetail(label: KLabel) {
    }

    override fun setAddLabelResult() {
    }

    override fun setDeleteLabelResult(id: String) {
        for ((index, label) in adapter.data.withIndex()) {
            if (id == label.id) {
                adapter.removeAt(index)
                break
            }
        }
        if (adapter.data.isEmpty()) {
            wkVBinding.noDataView.visibility = View.VISIBLE
        } else wkVBinding.noDataView.visibility = View.GONE
    }

    override fun getViewBinding(): ActLabelLayoutBinding {
        return ActLabelLayoutBinding.inflate(layoutInflater)
    }


    var chooseResultLac =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val type = result.data!!.getIntExtra("type", -1)
                if (type == 2) {
                    loadingPopup.show()
                    presenter.getLabels()
                } else if (type == 3) {
                    val id = result.data!!.getStringExtra("id")
                    setDeleteLabelResult(id!!)
                }
            }
        }
}
