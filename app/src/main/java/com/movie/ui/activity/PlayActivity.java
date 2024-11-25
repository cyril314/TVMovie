package com.movie.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private boolean isPause = false;
    private boolean isChangedState = true;
    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (isChangedState) {
                int mCurrentPosition = (int) mVideoView.getCurrentPosition();
                int mDuration = (int) mVideoView.getDuration();
                int progress = mDuration == 0 ? 0 : (int) (mCurrentPosition * 1.0 / mDuration * mVodSeekLayout.getMaxProgress());
                mVodSeekLayout.setProgress(progress);
                mVodSeekLayout.setCurrentPosition(mCurrentPosition);
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_play;
    }

    @Override
    protected void init() {
        initView();
        initData();
    }

    private void initView() {
        mVideoView = findViewById(R.id.mVideoView);
        tvHint = findViewById(R.id.tvHint);
        mProgressBar = findViewById(R.id.mProgressBar);
        mVodSeekLayout = findViewById(R.id.mVodSeekLayout);
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
                } else {
                    isPause = false;
                    mHandler.removeCallbacks(mRunnable);
                    mHandler.postDelayed(mRunnable, 1000);
                    mVideoView.resume();
                    mVodSeekLayout.start();
                }
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
        }
    }

    private float initialX, initialY; // 记录触摸事件的初始位置

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录触摸起始位置
                initialX = event.getX();
                initialY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                // 计算滑动的偏移量
                float deltaX = event.getX() - initialX;
                float deltaY = event.getY() - initialY;

                // 水平方向滑动
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > 50) { // 快进
                        fastForward();
                    } else if (deltaX < -50) { // 快退
                        fastRewind();
                    }
                } else { // 垂直方向或点击事件
                    if (Math.abs(deltaY) < 30) { // 点击事件
                        togglePlayPause();
                    }
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    // 暂停/播放切换
    private void togglePlayPause() {
        if (!isPause) {
            isPause = true;
            mHandler.removeCallbacks(mRunnable);  // 停止更新进度条
            mVideoView.pause();  // 暂停视频播放
            mVodSeekLayout.setVisibility(View.VISIBLE);
            int mCurrentPosition = (int) mVideoView.getCurrentPosition();
            int mDuration = (int) mVideoView.getDuration();
            int progress = mDuration == 0 ? 0 : mCurrentPosition * mVodSeekLayout.getMaxProgress() / mDuration;
            mVodSeekLayout.setProgress(progress);  // 更新进度条
            mVodSeekLayout.pause();  // 暂停进度条
        } else {
            isPause = false;
            mHandler.removeCallbacks(mRunnable);  // 停止更新进度条
            mHandler.postDelayed(mRunnable, 1000);  // 开始更新进度条
            mVideoView.resume();  // 恢复视频播放
            mVodSeekLayout.start();  // 恢复进度条
        }
    }

    // 快进功能
    private void fastForward() {
        int currentPosition = (int) mVideoView.getCurrentPosition();
        int duration = (int) mVideoView.getDuration();
        int newPosition = Math.min(currentPosition + 10000, duration); // 快进 10 秒
        mVideoView.seekTo(newPosition);
        updateSeekLayout(newPosition, duration);
    }

    // 快退功能
    private void fastRewind() {
        int currentPosition = (int) mVideoView.getCurrentPosition();
        int newPosition = Math.max(currentPosition - 10000, 0); // 快退 10 秒
        mVideoView.seekTo(newPosition);
        updateSeekLayout(newPosition, mVideoView.getDuration());
    }

    // 更新进度条布局
    private void updateSeekLayout(int currentPosition, long duration) {
        long progress = duration == 0 ? 0 : currentPosition * mVodSeekLayout.getMaxProgress() / duration;
        mVodSeekLayout.setVisibility(View.VISIBLE);
        mVodSeekLayout.setProgress((int) progress);
        mVodSeekLayout.setCurrentPosition(currentPosition);
        mVodSeekLayout.setDuration(duration);
    }
}