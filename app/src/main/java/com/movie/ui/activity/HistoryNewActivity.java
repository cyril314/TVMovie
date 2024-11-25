package com.movie.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.movie.R;
import com.movie.base.BaseActivity;
import com.movie.bean.VodInfo;
import com.movie.cache.RoomDataManger;
import com.movie.event.RefreshEvent;
import com.movie.ui.adapter.HistoryAdapter;
import com.movie.ui.dialog.HistoryDialog;
import com.movie.util.DefaultConfig;
import com.movie.util.FastClickCheckUtil;
import com.tv.leanback.GridLayoutManager;
import com.tv.leanback.OnItemListener;
import com.tv.leanback.VerticalGridView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aim
 * @date :2021/1/7
 * @description:
 */
public class HistoryNewActivity extends BaseActivity {
    private TextView tvTitle;
    private VerticalGridView mGridView;
    private HistoryAdapter historyAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_new_history;
    }

    @Override
    protected void init() {
        initView();
        initData();
    }

    private void initView() {
        EventBus.getDefault().register(this);
        tvTitle = findViewById(R.id.tvTitle);
        mGridView = findViewById(R.id.mGridView);
        mGridView.setHasFixedSize(true);
        historyAdapter = new HistoryAdapter();
        mGridView.setAdapter(historyAdapter);
        ((GridLayoutManager) mGridView.getLayoutManager()).setFocusOutAllowed(true, true);
        mGridView.setNumColumns(5);
        mGridView.setOnItemListener(new OnItemListener<VerticalGridView>() {
            @Override
            public void onItemSelected(VerticalGridView parent, View itemView, int position) {
                itemView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }

            @Override
            public void onItemPreSelected(VerticalGridView parent, View itemView, int position) {
                itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }
        });
        historyAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                VodInfo vodInfo = historyAdapter.getData().get(position);
                HistoryDialog historyDialog = new HistoryDialog().build(mContext)
                        .setVodInfo(vodInfo).setOnHistoryListener(new HistoryDialog.OnHistoryListener() {
                            @Override
                            public void onLook(VodInfo vodInfo) {
                                if (vodInfo != null) {
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("id", vodInfo.id);
                                    bundle.putString("sourceUrl", vodInfo.apiUrl);
                                    jumpActivity(DetailActivity.class, bundle);
                                }
                            }

                            @Override
                            public void onDelete(VodInfo vodInfo) {
                                if (vodInfo != null) {
                                    for (int i = 0; i < historyAdapter.getData().size(); i++) {
                                        if (vodInfo.id == historyAdapter.getData().get(i).id) {
                                            historyAdapter.remove(i);
                                            break;
                                        }
                                    }
                                    RoomDataManger.deleteVodRecord(vodInfo.apiUrl, vodInfo);
                                }
                            }
                        });
                historyDialog.show();
            }
        });
    }

    private void initData() {
        List<VodInfo> allVodRecord = RoomDataManger.getAllVodRecord();
        List<VodInfo> vodInfoList = new ArrayList<>();
        for (VodInfo vodInfo : allVodRecord) {
            if (!DefaultConfig.isContains(vodInfo.type)) {
                vodInfoList.add(vodInfo);
            }
        }
        historyAdapter.setNewData(vodInfoList);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent event) {
        if (event.type == RefreshEvent.TYPE_HISTORY_REFRESH) {
            initData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}