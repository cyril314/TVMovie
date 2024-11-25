package com.movie.ui.dialog;

import android.widget.TextView;

import com.movie.R;
import com.movie.base.BaseDialog;
import com.movie.bean.VodInfo;
import com.movie.util.FastClickCheckUtil;

/**
 * @author aim
 * @date :2020/12/23
 * @description: 历史记录对话框
 */
public class HistoryDialog extends BaseDialog<HistoryDialog> {
    private OnHistoryListener historyListener;
    private VodInfo vodInfo;

    public HistoryDialog setVodInfo(VodInfo vodInfo) {
        this.vodInfo = vodInfo;
        return this;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_history; // 返回特定布局资源 ID
    }

    @Override
    protected void init() {
        TextView tvLook = findViewById(R.id.tvLook);
        TextView tvDelete = findViewById(R.id.tvDelete);

        tvLook.setOnClickListener(v -> {
            FastClickCheckUtil.check(v);
            dismiss();
            if (historyListener != null) {
                historyListener.onLook(vodInfo);
            }
        });

        tvDelete.setOnClickListener(v -> {
            FastClickCheckUtil.check(v);
            dismiss();
            if (historyListener != null) {
                historyListener.onDelete(vodInfo);
            }
        });
    }

    public HistoryDialog setOnHistoryListener(OnHistoryListener listener) {
        this.historyListener = listener;
        return this;
    }

    public interface OnHistoryListener {
        void onLook(VodInfo vodInfo);
        void onDelete(VodInfo vodInfo);
    }
}
