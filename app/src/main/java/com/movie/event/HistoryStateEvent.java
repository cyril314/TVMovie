package com.movie.event;

/**
 * @author aim
 * @date :2020/12/21
 * @description:
 */
public class HistoryStateEvent {
    public final static int TYPE_TOP = 0;
    public int type;

    public HistoryStateEvent(int type) {
        this.type = type;
    }
}