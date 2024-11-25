package com.movie.ui.dialog;

import android.view.View;
import android.widget.TextView;

import com.movie.base.BaseDialog;
import com.orhanobut.hawk.Hawk;
import com.movie.R;
import com.movie.util.FastClickCheckUtil;
import com.movie.util.HawkConfig;

/**
 * @author aim
 * @date :2020/12/23
 * @description: 直播源对话框
 */
public class LiveSourceDialog extends BaseDialog<LiveSourceDialog> {
    private OnChangeLiveListener liveListener;

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_live_source;
    }

    @Override
    protected void init() {
        //直播源1 ip多余域名
        TextView tvLive1 = findViewById(R.id.tvLive1);
        //直播源2
        TextView tvLive2 = findViewById(R.id.tvLive2);
        int live = Hawk.get(HawkConfig.LIVE_SOURCE, 0);
        if (live == 0) {
            tvLive1.requestFocus();
            tvLive1.setTextColor(mContext.getResources().getColor(R.color.color_058AF4));
        } else {
            tvLive2.requestFocus();
            tvLive2.setTextColor(mContext.getResources().getColor(R.color.color_058AF4));
        }
        tvLive1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (live != 0 && liveListener != null) {
                    Hawk.put(HawkConfig.LIVE_SOURCE, 0);
                    liveListener.onChange();
                }
                dismiss();
            }
        });
        tvLive2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (live != 1 && liveListener != null) {
                    Hawk.put(HawkConfig.LIVE_SOURCE, 1);
                    liveListener.onChange();
                }
                dismiss();
            }
        });
    }

    public LiveSourceDialog setOnChangeLiveListener(OnChangeLiveListener listener) {
        liveListener = listener;
        return this;
    }

    public interface OnChangeLiveListener {
        void onChange();
    }
}