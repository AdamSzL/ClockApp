package com.example.clockapp.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.clockapp.db.daos.AlarmDao;
import com.example.clockapp.db.daos.ResultDao;
import com.example.clockapp.db.entities.Alarm;
import com.example.clockapp.db.entities.Result;

@Database(entities = {Alarm.class, Result.class}, version = 16)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlarmDao alarmDao();
    public abstract ResultDao resultDao();
}
