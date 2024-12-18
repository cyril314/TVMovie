package com.movie.bean;

import java.io.Serializable;

/**
 * @author aim
 * @date :2021/3/8
 * @description:
 */
public class PraseBean implements Serializable {

    private int id;
    private String praseName;
    private String praseUrl;
    public boolean selected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPraseName() {
        return praseName;
    }

    public void setPraseName(String praseName) {
        this.praseName = praseName;
    }

    public String getPraseUrl() {
        return praseUrl;
    }

    public void setPraseUrl(String praseUrl) {
        this.praseUrl = praseUrl;
    }
}