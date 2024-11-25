package com.movie.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.movie.R;

import java.util.ArrayList;

/**
 * @author aim
 * @date :2020/12/23
 * @description:
 */
public class SettingSortAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public SettingSortAdapter() {
        super(R.layout.item_setting_sort_layout, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tvName, item);
        helper.addOnClickListener(R.id.tvName);
    }
}