package com.movie.ui.dialog;

import com.movie.R;
import com.movie.base.BaseDialog;

/**
 * 描述
 *
 * @author aim
 * @since 2020/12/27
 */
public class LoadingDialog extends BaseDialog<LoadingDialog> {

    @Override
    protected int getLayoutResId() {
        return R.layout.loading_dialog;
    }

    @Override
    protected void init() {
    }
}
