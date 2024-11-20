package com.movie.ui.fragment;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.movie.api.ApiConfig;
import com.movie.base.BaseLazyFragment;
import com.movie.bean.PraseBean;
import com.movie.ui.adapter.PraseAdapter;
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
    private PraseAdapter praseAdapter;
    private final List<PraseBean> praseBeanList = new ArrayList<>();
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
        praseAdapter = new PraseAdapter();
        mGridView.setAdapter(praseAdapter);
        mGridView.setNumColumns(6);
        praseBeanList.addAll(ApiConfig.get().getPraseBeanList());
        for (int i = 0; i < praseBeanList.size(); i++) {
            if (praseBeanList.get(i).selected) {
                sourceIndex = i;
                break;
            }
        }
        praseAdapter.setNewData(praseBeanList);
        praseAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                if (sourceIndex != position) {
                    PraseBean praseBean = praseAdapter.getData().get(position);
                    praseAdapter.getData().get(sourceIndex).selected = false;
                    praseAdapter.notifyItemChanged(sourceIndex);
                    praseBean.selected = true;
                    praseAdapter.notifyItemChanged(position);
                    sourceIndex = position;
                    Hawk.put(HawkConfig.DEFAULT_PRASE_ID, praseBean.getId());
                }
            }
        });
    }
}