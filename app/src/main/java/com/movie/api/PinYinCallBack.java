package com.movie.api;

import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;

/**
 * @author aim
 * @date :2020/12/18
 * @description:
 */
public class PinYinCallBack<T> extends AbsCallback<T> {
    @Override
    public void onSuccess(Response<T> response) {

    }

    @Override
    public T convertResponse(okhttp3.Response response) throws Throwable {
        return null;
    }
}