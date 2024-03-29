package com.movie.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.movie.R;
import com.movie.bean.SourceBean;

import java.util.ArrayList;

/**
 * @author aim
 * @date :2021/2/5
 * @description:
 */
public class HistorySourceAdapter extends BaseQuickAdapter<SourceBean, BaseViewHolder> {
    public HistorySourceAdapter() {
        super(R.layout.item_home_sort_layout, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, SourceBean item) {
        helper.setText(R.id.tvTitle, item.getName());
        helper.addOnClickListener(R.id.tvTitle);
    }
}