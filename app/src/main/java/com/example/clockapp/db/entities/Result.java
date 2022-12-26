package com.example.clockapp.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "results")
public class Result {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "result")
    public long result;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getResult() {
        return result;
    }

    public void setResult(long result) {
        this.result = result;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Result(long result, long timestamp) {
        this.result = result;
        this.timestamp = timestamp;
    }
}
