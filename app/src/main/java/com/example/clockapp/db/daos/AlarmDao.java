package com.example.clockapp.db.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.clockapp.db.entities.Alarm;

import java.util.List;

@Dao
public interface AlarmDao {

    @Query("SELECT * FROM alarms")
    List<Alarm> getAll();

    @Query("DELETE FROM alarms where uid in (:idsToDelete)")
    public void deleteAlarms(List<Long> idsToDelete);

    @Update
    public void updateAlarm(Alarm alarm);

    @Insert
    long insertAlarm(Alarm alarm);
}
