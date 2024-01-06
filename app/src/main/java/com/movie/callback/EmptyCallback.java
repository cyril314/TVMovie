package com.movie.callback;

import com.kingja.loadsir.callback.Callback;
import com.movie.R;

/**
 * @author aim
 * @date :2020/12/24
 * @description:
 */
public class EmptyCallback extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.empty_layout;
    }
}