package com.movie.base;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.multidex.BuildConfig;
import androidx.multidex.MultiDexApplication;

import com.kingja.loadsir.core.LoadSir;
import com.movie.api.ApiConfig;
import com.movie.callback.EmptyCallback;
import com.movie.callback.LoadingCallback;
import com.movie.data.AppDataManager;
import com.movie.server.ControlManager;
import com.movie.util.AdBlocker;
import com.movie.util.CrashHandler;
import com.movie.util.HawkConfig;
import com.movie.util.ProgressManagerImpl;
import com.orhanobut.hawk.Hawk;
import com.tv.player.PlayerConfig;
import com.tv.player.PlayerFactory;
import com.tv.player.VideoView;
import com.tv.player.VideoViewManager;
import com.tv.player.android.AndroidMediaPlayer;
import com.tv.player.exo.ExoMediaPlayer;
import com.tv.player.ijk.IjkPlayer;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

/**
 * 基础配置
 *
 * @author aim
 * @date :2020/12/17
 * @description:
 */
public class App extends MultiDexApplication {

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ControlManager.init(this);
        //初始化数据库
        AppDataManager.init();
        LoadSir.beginBuilder()
                .addCallback(new EmptyCallback())
                .addCallback(new LoadingCallback())
                .commit();
        AutoSizeConfig.getInstance().getUnitsManager()
                .setSupportDP(false)
                .setSupportSP(false)
                .setSupportSubunits(Subunits.PT);
        initParams();
        AdBlocker.init(this);
        initPlay();
        Intent intent = new Intent();
        intent.setClassName(getPackageName(), getPackageName() + ".ui.activity.HomeActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext(), PendingIntent.getActivity(getApplicationContext(), 0, intent, 0));
    }

    /**
     * 初始化默认参数
     */
    private void initParams() {
        Hawk.init(this).build();
        if (!Hawk.contains(HawkConfig.PASSWORD)) { // 默认密码
            Hawk.put(HawkConfig.PASSWORD, HawkConfig.defaultPassword);
        }
        if (!Hawk.contains(HawkConfig.ADOLESCENT_MODEL)) { // 青少年模式
            Hawk.put(HawkConfig.ADOLESCENT_MODEL, true);
        }
        if (!Hawk.contains(HawkConfig.DEFAULT_SOURCE_ID)) {// 默认资源站
            Hawk.put(HawkConfig.DEFAULT_SOURCE_ID, 0);
        }
        if (!Hawk.contains(HawkConfig.MEDIA_CODEC)) { // 是否DEBUG模式
            Hawk.put(HawkConfig.MEDIA_CODEC, false);
        }
        if (!Hawk.contains(HawkConfig.PLAY_TYPE)) {// 播放器类型
            Hawk.put(HawkConfig.PLAY_TYPE, 0);
        }
        if (!Hawk.contains(HawkConfig.LIVE_CHANNEL)) { //播放位置不是频道号
            Hawk.put(HawkConfig.LIVE_CHANNEL, 0);
        }
        if (!Hawk.contains(HawkConfig.LIVE_SOURCE)) { //Ip多余域名
            Hawk.put(HawkConfig.LIVE_SOURCE, 0);
        }
        ApiConfig.get().loadSource(this);
    }

    private void initPlay() {
        int playType = Hawk.get(HawkConfig.PLAY_TYPE, 0);
        PlayerFactory playerFactory;
        if (playType == 1) {
            playerFactory = new PlayerFactory<IjkPlayer>() {
                @Override
                public IjkPlayer createPlayer(Context context) {
                    return new IjkPlayer(context);
                }
            };
        } else if (playType == 2) {
            playerFactory = new PlayerFactory<ExoMediaPlayer>() {
                @Override
                public ExoMediaPlayer createPlayer(Context context) {
                    return new ExoMediaPlayer(context);
                }
            };
        } else {
            playerFactory = new PlayerFactory<AndroidMediaPlayer>() {
                @Override
                public AndroidMediaPlayer createPlayer(Context context) {
                    return new AndroidMediaPlayer(context);
                }
            };
        }
        //播放器配置，注意：此为全局配置，按需开启
        VideoViewManager.setConfig(PlayerConfig.newBuilder()
                .setLogEnabled(BuildConfig.DEBUG)//调试的时候请打开日志，方便排错
                .setScreenScaleType(VideoView.SCREEN_SCALE_16_9)
                .setPlayerFactory(playerFactory)
                .setEnableMediaCodec(Hawk.get(HawkConfig.MEDIA_CODEC, false))
                .setProgressManager(new ProgressManagerImpl())
                .build());
    }

    public static App getInstance() {
        return instance;
    }
}