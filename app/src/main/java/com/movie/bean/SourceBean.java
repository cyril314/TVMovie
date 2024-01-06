package com.movie.bean;

/**
 * @author aim
 * @date :2020/12/18
 * @description:
 */
public class SourceBean {

    private int id; // 主键
    private String name; // 资源名称
    private String api; // 资源接口
    private String download; //下载接口
    public boolean selected = false; // 是否选中主页显示

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }
}