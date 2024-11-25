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
 * @description: 更改播放对话框
 */
public class ChangePlayDialog extends BaseDialog<ChangePlayDialog> {
    private OnChangePlayListener playListener;

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_change_play;
    }

    @Override
    public void init() {
        TextView tvSystem = findViewById(R.id.tvSystem);
        TextView tvIjk = findViewById(R.id.tvIjk);
        TextView tvExo = findViewById(R.id.tvExo);
        int playType = Hawk.get(HawkConfig.PLAY_TYPE, 0);
        if (playType == 1) {
            tvIjk.requestFocus();
            tvIjk.setTextColor(mContext.getResources().getColor(R.color.color_058AF4));
        } else if (playType == 2) {
            tvExo.requestFocus();
            tvExo.setTextColor(mContext.getResources().getColor(R.color.color_058AF4));
        } else {
            tvSystem.requestFocus();
            tvSystem.setTextColor(mContext.getResources().getColor(R.color.color_058AF4));
        }
        tvSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (playType != 0 && playListener != null) {
                    Hawk.put(HawkConfig.PLAY_TYPE, 0);
                    playListener.onChange();
                }
                dismiss();
            }
        });
        tvIjk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (playType != 1 && playListener != null) {
                    Hawk.put(HawkConfig.PLAY_TYPE, 1);
                    playListener.onChange();
                }
                dismiss();
            }
        });
        tvExo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (playType != 2 && playListener != null) {
                    Hawk.put(HawkConfig.PLAY_TYPE, 2);
                    playListener.onChange();
                }
                dismiss();
            }
        });
    }

    public ChangePlayDialog setOnChangePlayListener(OnChangePlayListener listener) {
        playListener = listener;
        return this;
    }

    public interface OnChangePlayListener {
        void onChange();
    }
}