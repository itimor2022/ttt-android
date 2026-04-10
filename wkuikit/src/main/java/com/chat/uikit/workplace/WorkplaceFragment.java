package com.chat.uikit.workplace;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.config.WKApiConfig;
import com.chat.base.base.WKBaseFragment;
import com.chat.base.glide.GlideUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.FragmentWorkplaceBinding;
import com.chat.uikit.workplace.bean.WorkplaceCategoryBean;

import java.util.List;

public class WorkplaceFragment  extends WKBaseFragment<FragmentWorkplaceBinding> implements WorkplaceContract.WorkplaceView {
    WorkplacePresenterImpl mWorkplacePresenter;

    BaseQuickAdapter<WorkplaceCategoryBean, BaseViewHolder> mAdapter;

    @Override
    protected void initPresenter() {
        super.initPresenter();

        wkVBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        wkVBinding.recyclerView.setAdapter(mAdapter = new BaseQuickAdapter<WorkplaceCategoryBean, BaseViewHolder>(R.layout.layout_find_item) {
            @Override
            protected void convert(@NonNull BaseViewHolder baseViewHolder, WorkplaceCategoryBean item) {
                ImageFilterView icon = baseViewHolder.getView(R.id.iv_icon);
                GlideUtils.getInstance().showImg(getContext(),WKApiConfig.baseUrl + item.cover,icon);
                baseViewHolder.setText(R.id.tv_content,item.title);
                baseViewHolder.getView(R.id.tv_copy).setOnClickListener(v->{
                    // 复制 route 到剪贴板
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip =
                            android.content.ClipData.newPlainText("Route", item.route);
                    clipboard.setPrimaryClip(clip);

                    // 显示提示
                    Toast.makeText(getContext(), R.string.copy_success, Toast.LENGTH_SHORT).show();
                });

//                baseViewHolder.itemView.setOnClickListener(v->{
//                    WKWebViewActivity.startAc(getContext(),item.route);
//                });
                baseViewHolder.itemView.setOnClickListener(v -> {
                    try {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setData(android.net.Uri.parse(item.route));
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "无法打开链接", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mWorkplacePresenter = new WorkplacePresenterImpl(this);
        mWorkplacePresenter.getWorkplaceCategory();

    }

    @Override
    public void getWorkplaceCategory(List<WorkplaceCategoryBean> userInfo) {
        mAdapter.setNewInstance(userInfo);
    }

    @Override
    protected FragmentWorkplaceBinding getViewBinding() {
        return FragmentWorkplaceBinding.inflate(getLayoutInflater());
    }

    @Override
    public void showError(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void hideLoading() {
    }
}