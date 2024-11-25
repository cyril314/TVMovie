package com.movie.ui.dialog;

import android.widget.ImageView;
import android.widget.TextView;

import com.movie.R;
import com.movie.base.BaseDialog;
import com.movie.server.RemoteServer;
import com.tv.QRCodeGen;

/**
 * @author aim
 * @date :2020/12/23
 * @description: 远程搜索对话框
 */
public class RemoteDialog extends BaseDialog<RemoteDialog> {
    private ImageView ivQRCode;
    private TextView tvAddress;

    @Override
    protected int getLayoutResId() {
        return R.layout.remote_dialog;
    }

    @Override
    protected void init() {
        ivQRCode = findViewById(R.id.ivQRCode);
        tvAddress = findViewById(R.id.tvAddress);
        refreshQRCode();
    }

    private void refreshQRCode() {
        String address = RemoteServer.getServerAddress(mContext);
        tvAddress.setText(String.format("手机/电脑扫描左边二维码或者直接浏览器访问地址\n%s", address));
        ivQRCode.setImageBitmap(QRCodeGen.generateBitmap(address, 200, 200));
    }
}
