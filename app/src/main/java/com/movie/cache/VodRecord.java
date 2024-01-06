package com.movie.cache;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * @author aim
 * @date :2021/1/7
 * @description:
 */
@Entity(tableName = "vodRecord")
public class VodRecord implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "vodId")
    public int vodId;
    @ColumnInfo(name = "updateTime")
    public long updateTime;
    @ColumnInfo(name = "apiUrl")
    public String apiUrl;
    public byte[] data;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}