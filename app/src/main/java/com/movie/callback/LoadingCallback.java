package com.movie.callback;

import android.content.Context;
import android.view.View;

import com.kingja.loadsir.callback.Callback;
import com.movie.R;

/**
 * @author aim
 * @date :2020/12/24
 * @description:
 */
public class LoadingCallback extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.loading_layout;
    }
}