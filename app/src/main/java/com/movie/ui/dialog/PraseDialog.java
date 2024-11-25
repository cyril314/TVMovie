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
 * @description:
 */
public class PraseDialog extends BaseDialog<PraseDialog> {
    private OnPraseListener playListener;

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_prase;
    }

    @Override
    protected void init() {
        // 获取 TextView 的 ID 列表
        int[] textViewIds = {R.id.tvPrase1, R.id.tvPrase2, R.id.tvPrase3, R.id.tvPrase4};
        // 当前选中的线路 ID
        int id = Hawk.get(HawkConfig.DEFAULT_PRASE_ID, 1);
        // 遍历所有线路 TextView
        for (int i = 0; i < textViewIds.length; i++) {
            TextView textView = findViewById(textViewIds[i]);
            int lineId = i + 1; // 线路 ID 从 1 开始
            // 如果当前线路是选中的，设置焦点和高亮
            if (id == lineId) {
                textView.requestFocus();
                textView.setTextColor(mContext.getResources().getColor(R.color.color_058AF4));
            }
            // 设置点击事件
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FastClickCheckUtil.check(v);
                    if (id != lineId && playListener != null) {
                        Hawk.put(HawkConfig.DEFAULT_PRASE_ID, lineId);
                        playListener.onChange();
                    }
                    dismiss();
                }
            });
        }
    }

    public PraseDialog setOnPraseListener(OnPraseListener listener) {
        playListener = listener;
        return this;
    }

    public interface OnPraseListener {
        void onChange();
    }
}