package com.movie.bean;

import java.io.Serializable;

/**
 * @author aim
 * @date :2021/2/5
 * @description:
 */
public class SearchRequest implements Serializable {
    public int index;
    public String api;
    public String name;

    public SearchRequest(int index, String api, String name) {
        this.index = index;
        this.api = api;
        this.name = name;
    }
}