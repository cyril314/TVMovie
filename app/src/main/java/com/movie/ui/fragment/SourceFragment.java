package com.movie.ui.fragment;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.movie.api.ApiConfig;
import com.movie.base.BaseLazyFragment;
import com.movie.bean.SourceBean;
import com.movie.ui.adapter.SettingSourceAdapter;
import com.movie.util.FastClickCheckUtil;
import com.movie.R;
import com.tv.leanback.VerticalGridView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aim
 * @date :2020/12/23
 * @description: 数据源
 */
public class SourceFragment extends BaseLazyFragment {
    private VerticalGridView mGridView;
    private SettingSourceAdapter settingAdapter;
    private final List<SourceBean> lists = new ArrayList<>();
    private int sourceIndex = 0;

    public static SourceFragment newInstance() {
        return new SourceFragment().setArguments();
    }

    public SourceFragment setArguments() {
        return this;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_source_grid;
    }

    @Override
    protected void init() {
        mGridView = findViewById(R.id.mGridView);
        settingAdapter = new SettingSourceAdapter();
        mGridView.setAdapter(settingAdapter);
        mGridView.setNumColumns(6);
        lists.addAll(ApiConfig.get().getSourceBeanList());
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).getId() == ApiConfig.get().getDefaultSourceBean().getId()) {
                lists.get(i).selected = true;
                sourceIndex = i;
                break;
            }
        }
        settingAdapter.setNewData(lists);
        settingAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                if (sourceIndex != position) {
                    SourceBean sourceBean = settingAdapter.getData().get(position);
                    settingAdapter.getData().get(sourceIndex).selected = false;
                    settingAdapter.notifyItemChanged(sourceIndex);
                    sourceBean.selected = true;
                    settingAdapter.notifyItemChanged(position);
                    sourceIndex = position;
                    ApiConfig.get().setSourceBean(sourceBean);
                }
            }
        });
    }
}