package com.movie.bean;

import java.io.Serializable;

/**
 * @author aim
 * @date :2021/1/5
 * @description:
 */
public class ApkInfo implements Serializable {

    private int versionCode; //版本号
    private String apkUrl; //更新地址

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }
}