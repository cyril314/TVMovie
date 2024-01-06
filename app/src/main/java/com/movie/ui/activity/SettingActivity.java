package com.movie.ui.activity;

import android.graphics.Color;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.movie.ui.adapter.SettingSortAdapter;
import com.orhanobut.hawk.Hawk;
import com.movie.R;
import com.movie.api.ApiConfig;
import com.movie.base.BaseActivity;
import com.movie.base.BaseLazyFragment;
import com.movie.ui.adapter.SettingPageAdapter;
import com.movie.ui.fragment.ModelSettingFragment;
import com.movie.ui.fragment.PraseFragment;
import com.movie.ui.fragment.SourceSettingFragment;
import com.movie.util.AppManager;
import com.movie.util.HawkConfig;
import com.tv.leanback.OnChildViewHolderSelectedListener;
import com.tv.leanback.OnItemListener;
import com.tv.leanback.VerticalGridView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aim
 * @date :2020/12/23
 * @description:
 */
public class SettingActivity extends BaseActivity {
    private VerticalGridView mGridView;
    private ViewPager mViewPager;
    private SettingSortAdapter sortAdapter;
    private SettingPageAdapter pageAdapter;
    private final List<BaseLazyFragment> fragments = new ArrayList<>();
    private boolean isRight = false;
    private boolean sortChange = false;
    private int defaultSelected = 0;
    private int sortFocused = 0;
    private final Handler mHandler = new Handler();
    private int id;
    private boolean adolescentDefault;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_setting;
    }

    @Override
    protected void init() {
        initView();
        initData();
    }

    private void initView() {
        mGridView = findViewById(R.id.mGridView);
        mViewPager = findViewById(R.id.mViewPager);
        sortAdapter = new SettingSortAdapter();
        mGridView.setAdapter(sortAdapter);
        sortAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.tvName) {
                    if (view.getParent() != null) {
                        ((ViewGroup) view.getParent()).requestFocus();
                        sortFocused = position;
                        if (sortFocused != defaultSelected) {
                            defaultSelected = sortFocused;
                            mViewPager.setCurrentItem(sortFocused, false);
                        }
                    }
                }
            }
        });
        mGridView.setOnItemListener(new OnItemListener<VerticalGridView>() {
            @Override
            public void onItemSelected(VerticalGridView parent, View itemView, int position) {
                if (itemView != null) {
                    sortChange = true;
                    sortFocused = position;
                    TextView tvName = itemView.findViewById(R.id.tvName);
                    tvName.setTextColor(Color.WHITE);
                }
            }

            @Override
            public void onItemPreSelected(VerticalGridView parent, View itemView, int position) {
                if (itemView != null) {
                    if (!isRight) {
                        TextView tvName = itemView.findViewById(R.id.tvName);
                        tvName.setTextColor(getResources().getColor(R.color.color_CCFFFFFF));
                    }
                }
            }
        });
        mGridView.setOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(RecyclerView parent, RecyclerView.ViewHolder viewHolder, int position, int subposition) {
                if (viewHolder != null && viewHolder.itemView != null) {
                    viewHolder.itemView.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                isRight = false;
                                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                                    isRight = true;
                                } else return keyCode == KeyEvent.KEYCODE_DPAD_LEFT && position == 0 || keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == sortAdapter.getData().size() - 1;
                            }
                            return false;
                        }
                    });
                }
            }
        });
    }

    private void initData() {
        id = ApiConfig.get().getDefaultSourceBean().getId();
        adolescentDefault = Hawk.get(HawkConfig.ADOLESCENT_MODEL, true);
        List<String> sortList = new ArrayList<>();
        sortList.add("首页数据源");
        sortList.add("解析线路");
        sortList.add("设置其他");
        sortAdapter.setNewData(sortList);
        initViewPager();
    }

    private void initViewPager() {
        fragments.add(SourceSettingFragment.newInstance());
        fragments.add(PraseFragment.newInstance());
        fragments.add(ModelSettingFragment.newInstance());
        pageAdapter = new SettingPageAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setCurrentItem(0);
    }

    private final Runnable mDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (sortChange) {
                sortChange = false;
                if (sortFocused != defaultSelected) {
                    defaultSelected = sortFocused;
                    mViewPager.setCurrentItem(sortFocused, false);
                }
            }
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            mHandler.removeCallbacks(mDataRunnable);
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            mHandler.postDelayed(mDataRunnable, 200);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (id != ApiConfig.get().getDefaultSourceBean().getId() || adolescentDefault != Hawk.get(HawkConfig.ADOLESCENT_MODEL, true)) {
            AppManager.getInstance().finishAllActivity();
            jumpActivity(HomeActivity.class);
        } else {
            super.onBackPressed();
        }
    }
}