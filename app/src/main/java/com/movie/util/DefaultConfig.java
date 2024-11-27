package com.movie.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.orhanobut.hawk.Hawk;
import com.movie.bean.MovieSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认设置
 *
 * @author aim
 * @date :2020/12/21
 * @description:
 */
public class DefaultConfig {

    private static final String DEFAULT_REMOVE = "连续剧,剧集,综艺,论理,伦理,倫理";

    /**
     * 调整排序
     */
    public static List<MovieSort.SortData> adjustSort(List<MovieSort.SortData> list) {
        List<MovieSort.SortData> data = new ArrayList<>();
        for (MovieSort.SortData sortData : list) {
            if (!isContains(sortData.name)) {
                data.add(sortData);
            }
        }
        data.add(0, new MovieSort.SortData(0, "我的"));
        Collections.sort(data);
        return data;
    }

    public static boolean isContains(String s) {
        boolean contains = false;
        String[] remove;
        if (!Hawk.get(HawkConfig.ADOLESCENT_MODEL, true)) {
            remove = DEFAULT_REMOVE.split(",");
        } else {
            remove = getRemove();
        }
        for (String temp : remove) {
            if (s.contains(temp)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private static String[] getRemove() {
        String del = DEFAULT_REMOVE + ",萝莉,福利,激情,理论,写真,情色,美女,街拍,赤足,性感,里番,av,AV,VIP,乱伦,人妻,巨乳," +
                "偷拍,无码,有码,3p,3P,颜射,口交,自慰,SM,sm,精品,诱惑,教师,大秀,字幕,性爱,色情,性交,同性,自拍,国产主播";
        return del.split(",");
    }

    public static int getAppVersionCode(Context mContext) {
        //包管理操作管理类
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getAppVersionName(Context mContext) {
        //包管理操作管理类
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取路径的文件名
     *
     * @param url
     * @return
     */
    public static String getFileName(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        Uri uri = Uri.parse(url);
        String path = uri.getPath();
        if (path != null) {
            int index = path.lastIndexOf("/") + 1;
            return path.substring(index);
        }
        return url;
    }

    /**
     * 后缀
     *
     * @param name
     * @return
     */
    public static String getFileSuffix(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        int endP = name.lastIndexOf(".");
        return endP > -1 ? name.substring(endP) : "";
    }

    /**
     * 获取文件的前缀
     *
     * @param fileName
     * @return
     */
    public static String getFilePrefixName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        int start = fileName.lastIndexOf(".");
        return start > -1 ? fileName.substring(0, start) : fileName;
    }
}