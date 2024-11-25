package com.movie.ui.dialog;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.movie.base.BaseDialog;
import com.orhanobut.hawk.Hawk;
import com.movie.R;
import com.movie.util.FastClickCheckUtil;
import com.movie.util.HawkConfig;

/**
 * @author aim
 * @date :2020/12/23
 * @description: 密码对话框
 */
public class ModelDialog extends BaseDialog<ModelDialog> {
    private EditText editText;
    private OnChangeModelListener modelListener;

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_model;
    }

    @Override
    protected void init() {
        editText = findViewById(R.id.etPassword);
        findViewById(R.id.tvConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                String pwd = editText.getText().toString().trim();
                String defaultPwd = Hawk.get(HawkConfig.PASSWORD);
                if (defaultPwd.equals(pwd)) {
                    if (modelListener != null) {
                        modelListener.onChangeModel();
                    }
                    mDialog.dismiss();
                } else {
                    Toast.makeText(mContext, "密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.tvCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    public ModelDialog setOnChangeModelListener(OnChangeModelListener listener) {
        modelListener = listener;
        return this;
    }

    public interface OnChangeModelListener {
        void onChangeModel();
    }
}