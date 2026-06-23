package com.movie.ui.activity;

import android.content.DialogInterface;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.os.Environment;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AlertDialog;

import com.movie.R;
import com.movie.api.ApiConfig;
import com.movie.base.BaseActivity;
import com.movie.bean.PraseBean;
import com.movie.bean.VodInfo;
import com.movie.event.RefreshEvent;
import com.movie.util.HawkConfig;
import com.movie.widget.VodPlayView;
import com.movie.widget.VodSeekLayout;
import com.orhanobut.hawk.Hawk;
import com.tv.player.VideoView;

import java.util.List;
import org.greenrobot.eventbus.EventBus;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.Session;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.ExecuteCallback;

import android.content.ContentValues;
import android.provider.MediaStore;
import android.media.MediaScannerConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

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
    private static final int REQUEST_STORAGE = 1001;
    private String pendingDownloadUrl;
    private String pendingDownloadFileName;


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
        updateScreenScaleType(getResources().getConfiguration().orientation);
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
            @Override
            public void onDownload() {
                if (mVodInfo == null) {
                    Toast.makeText(mContext, "当前无可下载的视频", Toast.LENGTH_SHORT).show();
                    return;
                }
                String url = mVodInfo.seriesList.get(mVideoView.getPlayIndex()).url;
                String name = mVodInfo.name + "_" + mVodInfo.seriesList.get(mVideoView.getPlayIndex()).name + ".mp4";
                pendingDownloadUrl = url;
                pendingDownloadFileName = name.replaceAll("[\\\\/:*?\"<>|]", "_");
                // 权限检查（Android Q 以下需要）
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(PlayActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PlayActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                        return;
                    }
                }
                performDownload(pendingDownloadUrl, pendingDownloadFileName);
            }
        });
        mVodSeekLayout.setToggleState(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
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

    private void performDownload(String url, String fileName) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(mContext, "下载地址为空", Toast.LENGTH_SHORT).show();
            return;
        }
        // 检测 m3u8 流，直接在设备上用 FFmpeg 转码为 MP4
        if (url.contains(".m3u8") || url.toLowerCase().contains("m3u8")) {
            transcodeM3u8ToMp4(url, fileName);
            return;
        }
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(fileName);
            request.setDescription("正在下载视频");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
                Toast.makeText(mContext, "已添加到下载列表，完成后请在下载目录查看", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "下载管理器不可用", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "下载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingDownloadUrl != null && pendingDownloadFileName != null) {
                    performDownload(pendingDownloadUrl, pendingDownloadFileName);
                }
            } else {
                Toast.makeText(this, "需要存储权限才能下载", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void transcodeM3u8ToMp4(final String url, final String fileName) {
        Toast.makeText(this, "开始转码(使用 FFmpeg)，请稍候...", Toast.LENGTH_LONG).show();
        final File outDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (outDir != null && !outDir.exists()) {
            outDir.mkdirs();
        }
        final File outFile = new File(outDir, fileName);
        final String ffmpegCmd = "-y -i \"" + url + "\" -c copy \"" + outFile.getAbsolutePath() + "\"";
        FFmpegKit.executeAsync(ffmpegCmd, new ExecuteCallback() {
            @Override
            public void apply(Session session) {
                ReturnCode returnCode = session.getReturnCode();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (returnCode != null && returnCode.isSuccess()) {
                            // 转码成功，尝试移动到公共 Downloads
                            boolean moved = moveToDownloads(outFile, fileName);
                            if (moved) {
                                Toast.makeText(PlayActivity.this, "转码并保存到下载目录成功: " + fileName, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(PlayActivity.this, "转码完成，文件保存在: " + outFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(PlayActivity.this, "转码失败", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private boolean moveToDownloads(File src, String displayName) {
        if (src == null || !src.exists()) return false;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                android.net.Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) return false;
                try (OutputStream os = getContentResolver().openOutputStream(uri); InputStream is = new FileInputStream(src)) {
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        os.write(buf, 0, len);
                    }
                }
                // 可选择删除 src
                src.delete();
                return true;
            } else {
                File dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), displayName);
                try (InputStream is = new FileInputStream(src); OutputStream os = new FileOutputStream(dest)) {
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        os.write(buf, 0, len);
                    }
                }
                MediaScannerConnection.scanFile(this, new String[]{dest.getAbsolutePath()}, new String[]{"video/mp4"}, null);
                src.delete();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
        new AlertDialog.Builder(this)
                .setTitle("选择解析源")
                .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedId = praseBeanList.get(which).getId();
                        if (selectedId != currentId) {
                            Hawk.put(HawkConfig.DEFAULT_PRASE_ID, selectedId);
                            Toast.makeText(PlayActivity.this, "解析源已切换，重新播放后生效", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (mVideoView.isPlaying()) {
                    mVodSeekLayout.setVisibility(View.VISIBLE);
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                mHandler.removeCallbacks(mRunnable);
                if (!isPause) {
                    isPause = true;
                    mVideoView.pause();
                    mVodSeekLayout.setVisibility(View.VISIBLE);
                    int mCurrentPosition = (int) mVideoView.getCurrentPosition();
                    int mDuration = (int) mVideoView.getDuration();
                    int progress = mDuration == 0 ? 0 : mCurrentPosition * mVodSeekLayout.getMaxProgress() / mDuration;
                    mVodSeekLayout.setProgress(progress);
                    mVodSeekLayout.pause();
                } else {
                    isPause = false;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 允许横竖屏切换，布局自动重绘，播放状态保持不变
        updateScreenScaleType(newConfig.orientation);
    }

    private void updateScreenScaleType(int orientation) {
        if (mVideoView == null) {
            return;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mVideoView.setScreenScaleType(VideoView.SCREEN_SCALE_DEFAULT);
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 使用 DEFAULT 确保视频完整显示，避免 CENTER_CROP 导致裁剪
            mVideoView.setScreenScaleType(VideoView.SCREEN_SCALE_DEFAULT);
        }
    }

    private float initialX; // 记录触摸事件的初始位置
    private float initialY;
    private boolean isTap = false;
    private static final int FAST_FORWARD_MILLIS = 15000;
    private static final int REWIND_MILLIS = 10000;

    private void seekBy(int offsetMillis) {
        int currentPosition = (int) mVideoView.getCurrentPosition();
        int duration = (int) mVideoView.getDuration();
        int newPosition = Math.max(0, Math.min(currentPosition + offsetMillis, duration));
        mVideoView.seekTo(newPosition);
        mVodSeekLayout.setVisibility(View.VISIBLE);
        int progress = duration == 0 ? 0 : (int) (newPosition * 1.0 / duration * mVodSeekLayout.getMaxProgress());
        mVodSeekLayout.setCurrentPosition(newPosition);
        mVodSeekLayout.setProgress(progress);
        if (isPause) {
            mVodSeekLayout.pause();
        } else {
            mVodSeekLayout.start();
        }
    }

    private boolean handleEdgeTap(float x) {
        int width = getWindow().getDecorView().getWidth();
        int edge = width / 5;
        if (x <= edge) {
            seekBy(-REWIND_MILLIS);
            return true;
        } else if (x >= width - edge) {
            seekBy(FAST_FORWARD_MILLIS);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialY = event.getY();
                isTap = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - initialX;
                float deltaY = event.getY() - initialY;
                if (Math.abs(deltaX) > 50 && Math.abs(deltaX) > Math.abs(deltaY)) { // 左右滑动阈值
                    isTap = false;
                    isPause = true;
                    mVodSeekLayout.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacks(mRunnable);
                    int mCurrentPosition = (int) mVideoView.getCurrentPosition();
                    int mDuration = (int) mVideoView.getDuration();
                    int offset = (int) (deltaX / 100 * 10000);
                    int newPosition = mCurrentPosition + offset;
                    newPosition = Math.max(0, Math.min(newPosition, mDuration));
                    mVideoView.seekTo(newPosition);
                    int progress = mDuration == 0 ? 0 : (int) (newPosition * 1.0 / mDuration * mVodSeekLayout.getMaxProgress());
                    mVodSeekLayout.setCurrentPosition(mVideoView.getCurrentPosition());
                    mVodSeekLayout.setProgress(progress);
                } else if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                    isTap = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isTap && handleEdgeTap(event.getX())) {
                    return true;
                }
                mHandler.removeCallbacks(mRunnable);
                if (!isPause) {
                    isPause = true;
                    mVideoView.pause();
                    mVodSeekLayout.setVisibility(View.VISIBLE);
                    int mCurrentPosition = (int) mVideoView.getCurrentPosition();
                    int mDuration = (int) mVideoView.getDuration();
                    int progress = mDuration == 0 ? 0 : mCurrentPosition * mVodSeekLayout.getMaxProgress() / mDuration;
                    mVodSeekLayout.setProgress(progress);
                    mVodSeekLayout.pause();
                } else {
                    isPause = false;
                    mHandler.postDelayed(mRunnable, 1000);
                    mVideoView.resume();
                    mVodSeekLayout.start();
                    if (mVideoView.getVisibility() == View.VISIBLE) {
                        mHandler.postDelayed(mRunnable, 5000);
                    }
                }
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }
}