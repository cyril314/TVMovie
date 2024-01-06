package com.movie.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.movie.cache.Cache;
import com.movie.cache.CacheDao;
import com.movie.cache.VodRecord;
import com.movie.cache.VodRecordDao;

/**
 * 类描述:
 *
 * @author aim
 * @since 2020/5/15
 */
@Database(entities = {Cache.class, VodRecord.class}, version = 1)
public abstract class AppDataBase extends RoomDatabase {
    public abstract CacheDao getCacheDao();

    public abstract VodRecordDao getVodRecordDao();
}
