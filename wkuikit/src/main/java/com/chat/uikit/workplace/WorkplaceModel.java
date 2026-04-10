package com.chat.uikit.workplace;

import android.util.Log;

import com.chat.base.base.WKBaseModel;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.IRequestResultErrorInfoListener;
import com.chat.base.net.IRequestResultListener;
import com.chat.uikit.workplace.bean.WorkplaceCategoryBean;

import java.util.List;

/**
 * @author:
 * @time: 2025/4/26 12:03
 * @describe:
 */
public class WorkplaceModel extends WKBaseModel {
    private WorkplaceModel() {
    }


    private static class WorkplaceModelBinder {
        private static final WorkplaceModel workModel = new WorkplaceModel();
    }

    public static WorkplaceModel getInstance() {
        return WorkplaceModelBinder.workModel;
    }

    public void getWorkplaceCategory(IWorkplaceListener listener) {
        request(createService(WorkplaceService.class).getWorkplaceCategory(), new IRequestResultListener<List<WorkplaceCategoryBean>>() {

            @Override
            public void onSuccess(List<WorkplaceCategoryBean> result) {
                listener.onResult(HttpResponseCode.success, "", result);
            }

            @Override
            public void onFail(int code, String msg) {
                listener.onResult(code, msg, null);
            }
        });
    }

    public interface IWorkplaceListener {
        void onResult(int code, String errorMsg, List<WorkplaceCategoryBean> userInfo);
    }
}
 