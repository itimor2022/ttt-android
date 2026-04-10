package com.chat.uikit.workplace;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import com.chat.base.base.WKBasePresenter;
import com.chat.base.base.WKBaseView;
import com.chat.base.entity.UserInfoEntity;
import com.chat.uikit.workplace.bean.WorkplaceCategoryBean;

import java.util.List;

/**
 * @author:
 * @time: 2025/4/26 11:45
 * @describe:
 */
public class WorkplaceContract {
    public interface WorkplacePresenter extends WKBasePresenter {
        void getWorkplaceCategory();
    }

    public interface WorkplaceView extends WKBaseView {
        void getWorkplaceCategory(List<WorkplaceCategoryBean> userInfo);
    }
}
 