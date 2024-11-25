package com.movie.ui.dialog;

import android.text.TextUtils;
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
 * @description:
 */
public class PasswordDialog extends BaseDialog<PasswordDialog> {
    private EditText oEditText;
    private EditText nEditText;
    private EditText cEditText;

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_password;
    }

    @Override
    protected void init() {
        oEditText = findViewById(R.id.etOPassword);
        nEditText = findViewById(R.id.etNPassword);
        cEditText = findViewById(R.id.etCPassword);
        findViewById(R.id.tvConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                String oPwd = oEditText.getText().toString().trim();
                String nPwd = nEditText.getText().toString().trim();
                String cPwd = cEditText.getText().toString().trim();
                String defaultPwd = Hawk.get(HawkConfig.PASSWORD);
                if (defaultPwd.equals(oPwd)) {
                    if (TextUtils.isEmpty(nPwd)) {
                        Toast.makeText(mContext, "新密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (nPwd.length() != 8) {
                        Toast.makeText(mContext, "密码必须为8位", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (nPwd.equals(oPwd)) {
                        Toast.makeText(mContext, "不能和原密码一致", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!nPwd.equals(cPwd)) {
                        Toast.makeText(mContext, "两次密码不一致", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Hawk.put(HawkConfig.PASSWORD, nPwd);
                    Toast.makeText(mContext, "密码修改成功", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                } else {
                    Toast.makeText(mContext, "原密码错误", Toast.LENGTH_SHORT).show();
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
}