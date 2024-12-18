package com.movie.bean;

import com.movie.util.DefaultConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频信息
 *
 * @author aim
 * @date :2020/12/22
 * @description:
 */
public class VodInfo implements Serializable {

    public String last;//时间
    public int id;
    /**
     * 视频分类ID
     */
    public int tid;
    /**
     * 影片名称
     */
    public String name;
    /**
     * 类型名称
     */
    public String type;
    //zuidam3u8,zuidall
    public String dt;
    /**
     * 图片
     */
    public String pic;
    /**
     * 语言
     */
    public String lang;
    /**
     * 地区
     */
    public String area;
    /**
     * 年份
     */
    public int year;
    public String state;
    /**
     * 描述集数或者影片信息
     */
    public String note;
    /**
     * 演员
     */
    public String actor;
    /**
     * 星级
     */
    public String director;
    public List<VodSeries> seriesList;
    public String des;// <![CDATA[权来]
    public boolean isX5;
    public int playIndex = 0;
    public String apiUrl;

    public void setVideo(Movie.Video video) {
        last = video.last;
        id = video.id;
        tid = video.tid;
        name = video.name;
        type = video.type;
        dt = video.dt;
        pic = video.pic;
        lang = video.lang;
        area = video.area;
        year = video.year;
        state = video.state;
        note = video.note;
        actor = video.actor;
        director = video.director;
        des = video.des;
        if (video.urlBean != null && video.urlBean.infoList != null && video.urlBean.infoList.size() > 0) {
            seriesList = new ArrayList<>();
            Movie.Video.UrlBean.UrlInfo mUrlInfo = null;
            for (Movie.Video.UrlBean.UrlInfo urlInfo : video.urlBean.infoList) {
                if (urlInfo.urls.toLowerCase().contains(".m3u8")) {
                    if (urlInfo.beanList != null && urlInfo.beanList.size() > 0) {
                        Movie.Video.UrlBean.UrlInfo.InfoBean infoBean = urlInfo.beanList.get(0);
                        String name = DefaultConfig.getFileName(infoBean.url);
                        if (name.contains(".") && !name.endsWith("html")) {
                            this.isX5 = false;
                            mUrlInfo = urlInfo;
                            break;
                        }
                        if (name.contains("$") && !name.endsWith("html")) {
                            this.isX5 = false;
                            mUrlInfo = urlInfo;
                            break;
                        }
                    }
                }
            }
            if (mUrlInfo == null) {
                this.isX5 = true;
                mUrlInfo = video.urlBean.infoList.get(0);
            }
            if (mUrlInfo.beanList != null && mUrlInfo.beanList.size() > 0) {
                for (Movie.Video.UrlBean.UrlInfo.InfoBean infoBean : mUrlInfo.beanList) {
                    seriesList.add(new VodSeries(infoBean.name, infoBean.url));
                }
            }
        }
    }

    public static class VodSeries implements Serializable {

        public String name;
        public String url;
        public boolean selected;

        public VodSeries() {
        }

        public VodSeries(String name, String url) {
            this.name = name;
            this.url = url.replaceAll("m3u8\\$.*$", "m3u8");
        }
    }
}