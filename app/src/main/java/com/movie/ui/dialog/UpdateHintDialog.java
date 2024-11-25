package com.movie.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IdRes;

import com.movie.R;
import com.movie.base.BaseDialog;

/**
 * @author aim
 * @date :2020/12/23
 * @description:
 */
public class UpdateHintDialog extends BaseDialog<UpdateHintDialog> {

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_update_hint;
    }

    @Override
    protected void init() {
    }
}