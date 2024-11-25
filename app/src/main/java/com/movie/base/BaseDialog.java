package com.movie.base;


import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IdRes;
import com.movie.R;
import com.movie.ui.dialog.LiveSourceDialog;

/**
 * @AUTO 基类对话框
 * @Author AIM
 * @DATE 2024/11/21
 */
public abstract class BaseDialog<T extends BaseDialog<T>> {
    protected View rootView;
    protected Dialog mDialog;
    protected Context mContext;

    // 修改 build 方法，允许不同的布局资源
    public T build(Context context) {
        rootView = LayoutInflater.from(context).inflate(getLayoutResId(), null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setContentView(rootView);
        mContext = context;
        init();
        return (T) this;
    }

    // 子类提供布局资源 ID
    protected abstract int getLayoutResId();

    // 子类实现初始化逻辑
    protected abstract void init();

    protected <T extends View> T findViewById(@IdRes int viewId) {
        if (rootView != null) {
            return rootView.findViewById(viewId);
        }
        return null;
    }

    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public boolean isShowing() {
        return mDialog != null && mDialog.isShowing();
    }
}