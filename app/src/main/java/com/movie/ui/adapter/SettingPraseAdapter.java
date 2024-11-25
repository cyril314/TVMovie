package com.movie.ui.adapter;

import android.graphics.Color;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.movie.R;
import com.movie.bean.PraseBean;

import java.util.ArrayList;

/**
 * @author aim
 * @date :2021/3/9
 * @description:
 */
public class SettingPraseAdapter extends BaseQuickAdapter<PraseBean, BaseViewHolder> {
    public SettingPraseAdapter() {
        super(R.layout.item_setting_prase_layout, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, PraseBean item) {
        TextView tvPrase = helper.getView(R.id.tvPrase);
        if (item.selected) {
            tvPrase.setTextColor(mContext.getResources().getColor(R.color.color_02F8E1));
        } else {
            tvPrase.setTextColor(Color.WHITE);
        }
        tvPrase.setText(item.getPraseName());
    }
}