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
    private boolean isPause = false;        //是否暂停
    private boolean isChangedState = true;  //是否改变状态
    private ProgressBar mProgressBar;       //进度条
    private TextView tvHint;                //视频标题
    private VodPlayView mVideoView;         //视频播放视图
    private VodSeekLayout mVodSeekLayout;   //视频点播搜索
    private VodInfo mVodInfo;
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

    private float initialX; // 记录触摸事件的初始位置

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                isPause = false;
                mHandler.removeCallbacks(mRunnable);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float deltaX = moveX - initialX;
                if (Math.abs(deltaX) > 50) { // 左右滑动阈值
                    isPause = true;
                    mVodSeekLayout.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacks(mRunnable);
                    // 获取当前播放位置（毫秒）
                    int mCurrentPosition = (int) mVideoView.getCurrentPosition();
                    // 获取视频总时长（毫秒）
                    int mDuration = (int) mVideoView.getDuration();
                    int offset = (int) (deltaX / 100 * 10000);
                    int newPosition = mCurrentPosition + offset;
                    newPosition = Math.max(0, Math.min(newPosition, mDuration)); // 限制范围
                    // 更新UI控件
                    mVideoView.seekTo(newPosition);
                    // 计算进度条进度（避免除零错误）
                    int progress = mDuration == 0 ? 0 : (int) (newPosition * 1.0 / mDuration * mVodSeekLayout.getMaxProgress());
                    mVodSeekLayout.setCurrentPosition(mVideoView.getCurrentPosition()); // 显示当前时间
                    mVodSeekLayout.setProgress(progress);// 设置进度条位置
                }
                break;
            case MotionEvent.ACTION_UP:
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
                // 重新设置自动隐藏逻辑
                if (mVideoView.getVisibility() == View.VISIBLE) {
                    mHandler.postDelayed(mRunnable, 5000);
                }
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }
}