package com.movie.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.movie.ui.adapter.LiveChannelAdapter;
import com.orhanobut.hawk.Hawk;
import com.movie.R;
import com.movie.api.ApiConfig;
import com.movie.base.BaseActivity;
import com.movie.bean.LiveChannel;
import com.movie.util.FastClickCheckUtil;
import com.movie.util.HawkConfig;
import com.movie.widget.LivePlayView;
import com.tv.leanback.VerticalGridView;
import com.tv.player.VideoView;
import com.tv.widget.ViewObj;

import java.util.List;

/**
 * @author aim
 * @date :2021/1/12
 * @description: 直播控制器
 */
public class LivePlayActivity extends BaseActivity {

    private LivePlayView mVideoView;
    private TextView tvHint;
    private ImageView ivLive;
    private TextView tvChannel;
    private ProgressBar mProgressBar;
    private VerticalGridView mGridView;
    private LiveChannelAdapter channelAdapter;
    private final Handler mHandler = new Handler();
    private int oldPlayIndex = 0;
    private String channelNum = "";
    private static final int AUTO_HIDE_DELAY = 10000; // 自动隐藏延迟时间
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            hideChannelList();
        }
    };
    private final Runnable playRunnable = new Runnable() {
        @Override
        public void run() {
            if (!TextUtils.isEmpty(channelNum)) {
                mVideoView.changeChannel(Integer.parseInt(channelNum));
                channelNum = "";
            }
            tvChannel.setText("");
            tvChannel.setVisibility(View.GONE);
        }
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_live_play;
    }

    @Override
    protected void init() {
        mVideoView = findViewById(R.id.mVideoView);
        mProgressBar = findViewById(R.id.mProgressBar);
        ivLive = findViewById(R.id.ivLive);
        mGridView = findViewById(R.id.mGridView);
        tvChannel = findViewById(R.id.tvChannel);
        tvHint = findViewById(R.id.tvHint);
        mGridView.setHasFixedSize(true);
        mVideoView.addOnStateChangeListener(new VideoView.OnSimpleStateChangeListener() {
            @Override
            public void OnPlayerState(int state) {
                switch (state) {
                    case VideoView.STATE_IDLE:
                        break;
                    case VideoView.STATE_PREPARED:
                    case VideoView.STATE_PLAYING:
                    case VideoView.STATE_BUFFERED:
                        mProgressBar.setVisibility(View.INVISIBLE);
                        break;
                    case VideoView.STATE_PAUSED:
                        break;
                    case VideoView.STATE_BUFFERING:
                    case VideoView.STATE_PREPARING:
                        mProgressBar.setVisibility(View.VISIBLE);
                        break;
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        break;
                    case VideoView.STATE_ERROR:
                        break;
                }
            }
        });
        mVideoView.setOnPlayIndexListener(new LivePlayView.OnPlayIndexListener() {
            @Override
            public void onPlayIndex(int playIndex) {
                if (oldPlayIndex != playIndex) {
                    channelAdapter.getData().get(oldPlayIndex).selected = false;
                    channelAdapter.getData().get(playIndex).selected = true;
                    channelAdapter.notifyDataSetChanged();
                    oldPlayIndex = playIndex;
                    mGridView.setFocusPosition(oldPlayIndex);
                    mGridView.scrollToPosition(playIndex);
                }
            }
        });
        channelAdapter = new LiveChannelAdapter();
        mGridView.setAdapter(channelAdapter);
        channelAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                if (oldPlayIndex != position) {
                    mVideoView.changeChannelIndex(position);
                    hideChannelList();
                }
            }
        });
        List<LiveChannel> channelList = ApiConfig.get().getChannelList();
        int index = Hawk.get(HawkConfig.LIVE_CHANNEL, 0);
        if (index >= channelList.size() || index < 0) {
            index = 0;
        }
        oldPlayIndex = index;
        channelList.get(index).selected = true;
        channelAdapter.setNewData(channelList);
        mGridView.scrollToPosition(index);
        mVideoView.setChannel(channelList);
        mHandler.postDelayed(mRunnable, AUTO_HIDE_DELAY);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && mGridView.getVisibility() == View.INVISIBLE) {
                mVideoView.playNext();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && mGridView.getVisibility() == View.INVISIBLE) {
                mVideoView.playPrevious();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE && mGridView.getVisibility() == View.INVISIBLE) {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    ivLive.setSelected(true);
                    ivLive.setVisibility(View.VISIBLE);
                } else {
                    ivLive.setSelected(false);
                    ivLive.setVisibility(View.INVISIBLE);
                    mVideoView.resume();
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && mGridView.getVisibility() == View.INVISIBLE) {
                showChannelList();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && mGridView.getVisibility() == View.VISIBLE) {
                hideChannelList();
            } else if (mGridView.getVisibility() == View.INVISIBLE) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_0:
                        changeNum("0");
                        break;
                    case KeyEvent.KEYCODE_1:
                        changeNum("1");
                        break;
                    case KeyEvent.KEYCODE_2:
                        changeNum("2");
                        break;
                    case KeyEvent.KEYCODE_3:
                        changeNum("3");
                        break;
                    case KeyEvent.KEYCODE_4:
                        changeNum("4");
                        break;
                    case KeyEvent.KEYCODE_5:
                        changeNum("5");
                        break;
                    case KeyEvent.KEYCODE_6:
                        changeNum("6");
                        break;
                    case KeyEvent.KEYCODE_7:
                        changeNum("7");
                        break;
                    case KeyEvent.KEYCODE_8:
                        changeNum("8");
                        break;
                    case KeyEvent.KEYCODE_9:
                        changeNum("9");
                        break;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void changeNum(String add) {
        if (channelNum.length() < 2) {
            mHandler.removeCallbacks(playRunnable);
            tvChannel.setVisibility(View.VISIBLE);
            channelNum = String.format("%s%s", channelNum, add);
            tvChannel.setText(channelNum);
            mHandler.postDelayed(playRunnable, 1000);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            mHandler.removeCallbacks(mRunnable);
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            if (mGridView.getVisibility() == View.VISIBLE) {
                mHandler.postDelayed(mRunnable, AUTO_HIDE_DELAY);
            }
        }
        return super.dispatchKeyEvent(event);
    }


    private void showChannelList() {
        mGridView.setVisibility(View.VISIBLE);
        ViewObj viewObj = new ViewObj(mGridView, (ViewGroup.MarginLayoutParams) mGridView.getLayoutParams());
        ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), -mGridView.getLayoutParams().width, 0);
        animator.setDuration(200);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mHandler.postDelayed(mRunnable, AUTO_HIDE_DELAY);
            }
        });
        tvHint.setVisibility(View.VISIBLE);
    }

    private void hideChannelList() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mGridView.getLayoutParams();
        if (mGridView.getVisibility() == View.VISIBLE) {
            ViewObj viewObj = new ViewObj(mGridView, params);
            ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), 0, -mGridView.getLayoutParams().width);
            animator.setDuration(200);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mGridView.setVisibility(View.INVISIBLE);
                }
            });
            animator.start();
            mGridView.scrollToPosition(mVideoView.getPlayIndex());
        }
        tvHint.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.resume();
        }
        if (mGridView.getVisibility() == View.VISIBLE) {
            mHandler.postDelayed(mRunnable, AUTO_HIDE_DELAY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
        mHandler.removeCallbacksAndMessages(null);
        tvChannel.setVisibility(View.GONE);
        tvChannel.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
        }
    }

    private float startX, startY;
    private boolean hasSwiped = false; // 标记滑动状态

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                mHandler.removeCallbacks(mRunnable); // 清除自动隐藏任务
                hasSwiped = false; // 每次触摸重置滑动标记
                return true;
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - startX;
                float deltaY = event.getY() - startY;
                // 限制滑动的方向，避免误触
                if (!hasSwiped && Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 50) {
                    if (deltaX > 50) { // 向右滑动，下一频道
                        mVideoView.playNext();
                    } else if (deltaX < -50) { // 向左滑动，上一频道
                        mVideoView.playPrevious();
                    }
                    hasSwiped = true; // 标记已滑动，避免多次触发
                }
                return true;
            case MotionEvent.ACTION_UP:
                // 处理单击逻辑
                if (!hasSwiped && Math.abs(event.getX() - startX) < 10 && Math.abs(event.getY() - startY) < 10) {
                    if (mGridView.getVisibility() == View.INVISIBLE) {
                        showChannelList();
                    }
                }
                // 重新设置自动隐藏逻辑
                if (mGridView.getVisibility() == View.VISIBLE) {
                    mHandler.postDelayed(mRunnable, 5000);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }
}