package com.movie.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.movie.R;
import com.movie.base.BaseActivity;
import com.movie.bean.VodInfo;
import com.movie.event.RefreshEvent;
import com.movie.widget.VodPlayView;
import com.movie.widget.VodSeekLayout;
import com.tv.player.VideoView;
import org.greenrobot.eventbus.EventBus;

/**
 * @author aim
 * @date :2020/12/22
 * @description: 视频控制器
 */
public class PlayActivity extends BaseActivity {
    private VodPlayView mVideoView;//视频播放视图
    private TextView tvHint; // 视频标题
    private ProgressBar mProgressBar; //进度条
    private VodSeekLayout mVodSeekLayout; //视频点播搜索
    private VodInfo mVodInfo;
    private boolean isPause = false; // 是否暂停
    private boolean isChangedState = true; //
    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (isChangedState) {
                int mCurrentPosition = (int) mVideoView.getCurrentPosition(); // 播放当前位置
                int mDuration = (int) mVideoView.getDuration(); // 获取视频总时长
                int progress = mDuration == 0 ? 0 : (int) (mCurrentPosition * 1.0 / mDuration * mVodSeekLayout.getMaxProgress());
                mVodSeekLayout.setProgress(progress);
                mVodSeekLayout.setCurrentPosition(mCurrentPosition);
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, 1000);
            }
        }
    };
    // 虚拟遥控器
    private LinearLayout virtualRemote;
    private Button btnRewind, btnPlayPause, btnForward;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_play;
    }

    @Override
    protected void init() {
        initView();
        initData();
        initVirtualRemote();
    }

    private void initView() {
        mVideoView = findViewById(R.id.mVideoView);
        tvHint = findViewById(R.id.tvHint);
        mProgressBar = findViewById(R.id.mProgressBar);
        mVodSeekLayout = findViewById(R.id.mVodSeekLayout);
        virtualRemote = findViewById(R.id.virtualRemote);
        btnRewind = findViewById(R.id.btnRewind);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnForward = findViewById(R.id.btnForward);
        mVideoView.addOnStateChangeListener(new VideoView.OnSimpleStateChangeListener() {
            @Override
            public void OnPlayerState(int state) {
                switch (state) {
                    case VideoView.STATE_IDLE:
                        break;
                    case VideoView.STATE_PREPARED:
                    case VideoView.STATE_PLAYING:
                        mHandler.post(mRunnable);
                        mVodSeekLayout.start();
                        mVodSeekLayout.setDuration(mVideoView.getDuration());
                        updatePlayPauseButton(true);
                        break;
                    case VideoView.STATE_BUFFERED:
                        mProgressBar.setVisibility(View.INVISIBLE);
                        break;
                    case VideoView.STATE_PAUSED:
                        updatePlayPauseButton(false);
                        break;
                    case VideoView.STATE_BUFFERING:
                    case VideoView.STATE_PREPARING:
                        mProgressBar.setVisibility(View.VISIBLE);
                        break;
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        if (mVideoView.hasNext()) {
                            mVideoView.playNext();
                            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_REFRESH, mVideoView.getPlayIndex()));
                            mVodSeekLayout.setVodName(String.format("%s[%s]", mVodInfo.name, mVodInfo.seriesList.get(mVideoView.getPlayIndex()).name));
                        }
                        mHandler.removeCallbacks(mRunnable);
                        mVodSeekLayout.setVisibility(View.VISIBLE);
                        mVodSeekLayout.setProgress(0);
                        mVodSeekLayout.setCurrentPosition(0);
                        mVodSeekLayout.setDuration(0);
                        mVodSeekLayout.pause();
                        break;
                    case VideoView.STATE_ERROR:
                        Toast.makeText(mContext, "播放错误", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        mVodSeekLayout.setOnSeekStateListener(new VodSeekLayout.OnSeekStateListener() {
            @Override
            public void onSeekState(int state, int progress) {
                if (state == VodSeekLayout.SEEK_START) {
                    isChangedState = false;
                    mHandler.removeCallbacks(mRunnable);
                } else if (state == VodSeekLayout.SEEK_STOP) {
                    mVideoView.seekTo(progress * mVideoView.getDuration() / mVodSeekLayout.getMaxProgress());
                    isChangedState = true;
                    mHandler.removeCallbacks(mRunnable);
                    mHandler.postDelayed(mRunnable, 1000);
                }
            }

            @Override
            public void onShowState(boolean show) {
                tvHint.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void initVirtualRemote() {
        // 显示/隐藏虚拟遥控器（点击屏幕切换）
        mVideoView.setOnClickListener(v -> {
            if (virtualRemote.getVisibility() == View.VISIBLE) {
                virtualRemote.setVisibility(View.GONE);
            } else {
                virtualRemote.setVisibility(View.VISIBLE);
                // 3秒后自动隐藏
                mHandler.removeCallbacks(hideRemoteRunnable);
                mHandler.postDelayed(hideRemoteRunnable, 3000);
            }
        });

        // 快退按钮（左）
        btnRewind.setOnClickListener(v -> {
            simulateKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
            resetHideTimer();
        });

        // 播放/暂停按钮
        btnPlayPause.setOnClickListener(v -> {
            simulateKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER);
            resetHideTimer();
        });

        // 快进按钮（右）
        btnForward.setOnClickListener(v -> {
            simulateKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
            resetHideTimer();
        });
    }

    private Runnable hideRemoteRunnable = new Runnable() {
        @Override
        public void run() {
            virtualRemote.setVisibility(View.GONE);
        }
    };

    private void resetHideTimer() {
        mHandler.removeCallbacks(hideRemoteRunnable);
        mHandler.postDelayed(hideRemoteRunnable, 3000);
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            btnPlayPause.setText("⏸");
        } else {
            btnPlayPause.setText("▶");
        }
    }

    private void simulateKeyEvent(int keyCode) {
        onKeyDown(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            mVodInfo = (VodInfo) bundle.getSerializable("VodInfo");
            mVideoView.setVodInfo(mVodInfo, mVodInfo.playIndex);
            mVodSeekLayout.setVodName(String.format("%s[%s]", mVodInfo.name, mVodInfo.seriesList.get(mVodInfo.playIndex).name));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (mVideoView.isPlaying()) {
                    mVodSeekLayout.setVisibility(View.VISIBLE);
                    // 显示虚拟遥控器
                    virtualRemote.setVisibility(View.VISIBLE);
                    resetHideTimer();
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (!isPause) {
                    isPause = true;
                    mHandler.removeCallbacks(mRunnable);
                    mVideoView.pause();
                    mVodSeekLayout.setVisibility(View.VISIBLE);
                    int mCurrentPosition = (int) mVideoView.getCurrentPosition();
                    int mDuration = (int) mVideoView.getDuration();
                    int progress = mDuration == 0 ? 0 : mCurrentPosition * mVodSeekLayout.getMaxProgress() / mDuration;
                    mVodSeekLayout.setProgress(progress);
                    mVodSeekLayout.pause();
                    updatePlayPauseButton(false);
                } else {
                    isPause = false;
                    mHandler.removeCallbacks(mRunnable);
                    mHandler.postDelayed(mRunnable, 1000);
                    mVideoView.resume();
                    mVodSeekLayout.start();
                    updatePlayPauseButton(true);
                }
                // 显示虚拟遥控器
                virtualRemote.setVisibility(View.VISIBLE);
                resetHideTimer();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (mVideoView.hasPrevious()) {
                    mVideoView.playPrevious();
                    mVodInfo.playIndex = mVideoView.getPlayIndex();
                    EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_REFRESH, mVideoView.getPlayIndex()));
                    mVodSeekLayout.setVodName(String.format("%s[%s]", mVodInfo.name, mVodInfo.seriesList.get(mVideoView.getPlayIndex()).name));
                    mHandler.removeCallbacks(mRunnable);
                    mVodSeekLayout.setVisibility(View.VISIBLE);
                    mVodSeekLayout.setProgress(0);
                    mVodSeekLayout.setCurrentPosition(0);
                    mVodSeekLayout.setDuration(0);
                    mVodSeekLayout.pause();
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (mVideoView.hasNext()) {
                    mVideoView.playNext();
                    mVodInfo.playIndex = mVideoView.getPlayIndex();
                    EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_REFRESH, mVideoView.getPlayIndex()));
                    mVodSeekLayout.setVodName(String.format("%s[%s]", mVodInfo.name, mVodInfo.seriesList.get(mVideoView.getPlayIndex()).name));
                    mHandler.removeCallbacks(mRunnable);
                    mVodSeekLayout.setVisibility(View.VISIBLE);
                    mVodSeekLayout.setProgress(0);
                    mVodSeekLayout.setCurrentPosition(0);
                    mVodSeekLayout.setDuration(0);
                    mVodSeekLayout.pause();
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
        mHandler.removeCallbacks(mRunnable);
        mHandler.removeCallbacks(hideRemoteRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
        }
        mHandler.removeCallbacks(mRunnable);
        mHandler.removeCallbacks(hideRemoteRunnable);
    }
}