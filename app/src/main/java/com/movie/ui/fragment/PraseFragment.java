package com.movie.ui.fragment;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.movie.api.ApiConfig;
import com.movie.base.BaseLazyFragment;
import com.movie.bean.PraseBean;
import com.movie.ui.adapter.SettingPraseAdapter;
import com.movie.util.FastClickCheckUtil;
import com.movie.util.HawkConfig;
import com.orhanobut.hawk.Hawk;
import com.movie.R;
import com.tv.leanback.VerticalGridView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aim
 * @date :2020/12/23
 * @description: 解析线路
 * "praseName": "虾米解析",
 * "praseUrl": "https://jx.xmflv.com/?url="
 */
public class PraseFragment extends BaseLazyFragment {
    private VerticalGridView mGridView;
    private SettingPraseAdapter settingAdapter;
    private final List<PraseBean> lists = new ArrayList<>();
    private int sourceIndex = 0;

    public static PraseFragment newInstance() {
        return new PraseFragment().setArguments();
    }

    public PraseFragment setArguments() {
        return this;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_source_grid;
    }

    @Override
    protected void init() {
        mGridView = findViewById(R.id.mGridView);
        settingAdapter = new SettingPraseAdapter();
        mGridView.setAdapter(settingAdapter);
        mGridView.setNumColumns(6);
        lists.addAll(ApiConfig.get().getPraseBeanList());
        sourceIndex = Hawk.get(HawkConfig.DEFAULT_PRASE_ID, 0);
        settingAdapter.setNewData(lists);
        settingAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                if (sourceIndex != position) {
                    PraseBean praseBean = settingAdapter.getData().get(position);
                    settingAdapter.getData().get(sourceIndex).selected = false;
                    settingAdapter.notifyItemChanged(sourceIndex);
                    praseBean.selected = true;
                    settingAdapter.notifyItemChanged(position);
                    sourceIndex = position;
                    Hawk.put(HawkConfig.DEFAULT_PRASE_ID, praseBean.getId());
                }
            }
        });
    }
}
