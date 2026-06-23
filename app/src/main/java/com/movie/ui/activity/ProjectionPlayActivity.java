package com.movie.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.movie.R;
import com.movie.api.ApiConfig;
import com.movie.base.BaseActivity;
import com.movie.bean.PraseBean;
import com.movie.util.HawkConfig;
import com.movie.widget.VodSeekLayout;
import com.orhanobut.hawk.Hawk;
import com.tv.player.VideoView;

import java.util.List;

/**
 * @author aim
 * @date :2021/3/5
 * @description: 投屏
 */
public class ProjectionPlayActivity extends BaseActivity {
    private String playUrl;
    private boolean isPause = false;
    private boolean isChangedState = true;
    private ProgressBar mProgressBar;
    private VideoView mVideoView;
    private VodSeekLayout mVodSeekLayout;
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
        return R.layout.activity_projection_play;
    }

    @Override
    protected void init() {
        initView();
        initData();
    }

    private void initView() {
        mVideoView = findViewById(R.id.mVideoView);
        mProgressBar = findViewById(R.id.mProgressBar);
        mVodSeekLayout = findViewById(R.id.mVodCastLayout);
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
                        mHandler.removeCallbacks(mRunnable);
                        mVodSeekLayout.setVisibility(View.VISIBLE);
                        mVodSeekLayout.setProgress(0);
                        mVodSeekLayout.setCurrentPosition(0);
                        mVodSeekLayout.setDuration(0);
                        mVodSeekLayout.pause();
                        break;
                    case VideoView.STATE_ERROR:
                        finish();
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
            }

            @Override
            public void onToggleScreen() {
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mVodSeekLayout.setToggleState(false);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mVodSeekLayout.setToggleState(true);
                }
            }

            @Override
            public void onSoundToggle() {
                boolean mute = !mVideoView.isMute();
                mVideoView.setMute(mute);
            }

            @Override
            public void onChangeSource() {
                showPraseSourceChooser();
            }
        });
        mVodSeekLayout.setToggleState(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    private void showPraseSourceChooser() {
        final List<PraseBean> praseBeanList = ApiConfig.get().getPraseBeanList();
        if (praseBeanList == null || praseBeanList.isEmpty()) {
            Toast.makeText(mContext, "暂无解析源可用", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = new String[praseBeanList.size()];
        int currentId = Hawk.get(HawkConfig.DEFAULT_PRASE_ID, 0);
        int checkedItem = 0;
        for (int i = 0; i < praseBeanList.size(); i++) {
            items[i] = praseBeanList.get(i).getPraseName();
            if (praseBeanList.get(i).getId() == currentId) {
                checkedItem = i;
            }
        }
        final int[] selectedIndex = new int[]{checkedItem};
        new AlertDialog.Builder(this)
                .setTitle("选择解析源")
                .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedIndex[0] = which;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int index = selectedIndex[0];
                        if (index >= 0 && index < praseBeanList.size()) {
                            int selectedId = praseBeanList.get(index).getId();
                            if (selectedId != currentId) {
                                Hawk.put(HawkConfig.DEFAULT_PRASE_ID, selectedId);
                                Toast.makeText(ProjectionPlayActivity.this, "解析源已切换，重新播放后生效", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            playUrl = bundle.getString("playUrl");
            mVodSeekLayout.setVodName("播放地址:" + playUrl);
            mVideoView.setUrl(playUrl);
            mVideoView.start();
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
}