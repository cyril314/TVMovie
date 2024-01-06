package com.movie.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

/**
 * @author aim
 * @date :2020/12/18
 * @description:
 */
@XStreamAlias("rss")
public class AbsXml implements Serializable {
    @XStreamAlias("list")
    public Movie movie;
    public String api;
}