package com.example.clockapp.db.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.clockapp.db.entities.Result;

import java.util.List;

@Dao
public interface ResultDao {

    @Query("SELECT * FROM results")
    List<Result> getAll();

    @Query("SELECT * FROM results ORDER BY result ASC LIMIT :count")
    public List<Result> getBestResults(int count);

    @Query("DELETE FROM results where uid in (:idsToDelete)")
    public void deleteResults(List<Long> idsToDelete);

    @Query("DELETE FROM results")
    public void deleteAll();

    @Update
    public void updateResult(Result result);

    @Insert
    long insertResult(Result result);
}
