package com.movie.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
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
    private static final int REQUEST_STORAGE = 1002;
    private String pendingDownloadUrl;
    private String pendingDownloadFileName;

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

            @Override
            public void onDownload() {
                if (playUrl == null || playUrl.isEmpty()) {
                    Toast.makeText(mContext, "当前无可下载的视频", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = "video_" + System.currentTimeMillis() + ".mp4";
                pendingDownloadUrl = playUrl;
                pendingDownloadFileName = name.replaceAll("[\\\\/:*?\"<>|]", "_");
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(ProjectionPlayActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        androidx.core.app.ActivityCompat.requestPermissions(ProjectionPlayActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                        return;
                    }
                }
                performDownload(pendingDownloadUrl, pendingDownloadFileName);
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
        new AlertDialog.Builder(this)
                .setTitle("选择解析源")
                .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedId = praseBeanList.get(which).getId();
                        if (selectedId != currentId) {
                            Hawk.put(HawkConfig.DEFAULT_PRASE_ID, selectedId);
                            Toast.makeText(ProjectionPlayActivity.this, "解析源已切换，重新播放后生效", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                })
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

    private void performDownload(String url, String fileName) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(mContext, "下载地址为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (url.contains(".m3u8") || url.toLowerCase().contains("m3u8")) {
            transcodeM3u8ToMp4(url, fileName);
            return;
        }
        try {
            android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(android.net.Uri.parse(url));
            request.setTitle(fileName);
            request.setDescription("正在下载视频");
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, fileName);
            android.app.DownloadManager dm = (android.app.DownloadManager) getSystemService(DOWNLOAD_SERVICE);
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
                            boolean moved = moveToDownloads(outFile, fileName);
                            if (moved) {
                                Toast.makeText(ProjectionPlayActivity.this, "转码并保存到下载目录成功: " + fileName, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ProjectionPlayActivity.this, "转码完成，文件保存在: " + outFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(ProjectionPlayActivity.this, "转码失败", Toast.LENGTH_LONG).show();
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