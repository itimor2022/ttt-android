package com.chat.uikit.workplace;

import com.chat.uikit.workplace.bean.WorkplaceCategoryBean;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;

/**
 * @author:
 * @time: 2025/4/26 12:09
 * @describe:
 */
public interface WorkplaceService {
    @GET("workplace/banner")
    Observable<List<WorkplaceCategoryBean>> getWorkplaceCategory();
}
