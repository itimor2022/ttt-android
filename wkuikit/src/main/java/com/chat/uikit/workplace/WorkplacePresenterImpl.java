package com.chat.uikit.workplace;

import com.chat.base.entity.UserInfoEntity;
import com.chat.base.net.HttpResponseCode;

import java.lang.ref.WeakReference;

/**
 * @author:
 * @time: 2025/4/26 11:52
 * @describe:
 */
public class WorkplacePresenterImpl implements WorkplaceContract.WorkplacePresenter {
    private final WeakReference<WorkplaceContract.WorkplaceView> mView;

    public WorkplacePresenterImpl(WorkplaceContract.WorkplaceView viewWeakReference) {
        mView = new WeakReference<>(viewWeakReference);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void getWorkplaceCategory() {
        WorkplaceModel.getInstance().getWorkplaceCategory((code, errorMsg, userInfo) -> {
            if(code == HttpResponseCode.success){
                if(mView.get()!=null){
                    mView.get().hideLoading();
                    mView.get().getWorkplaceCategory(userInfo);
                }
            }
        });
    }
}
 