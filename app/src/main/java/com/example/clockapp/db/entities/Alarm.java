package com.example.clockapp.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "alarms")
public class Alarm implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "enabled")
    public boolean enabled;

    @ColumnInfo(name = "selected")
    public boolean selected;

    @ColumnInfo(name = "time")
    public String time;

    @ColumnInfo(name = "label")
    public String label;

    @ColumnInfo(name = "repeat")
    public String repeatMode;

    @ColumnInfo(name = "ringtone")
    public String ringtoneUri;

    @ColumnInfo(name = "ringtoneStart")
    public float ringtoneStart;

    @ColumnInfo(name = "ringtoneEnd")
    public float ringtoneEnd;

    @ColumnInfo(name = "volume")
    public float volume;

    public float getRingtoneStart() {
        return ringtoneStart;
    }

    public void setRingtoneStart(float ringtoneStart) {
        this.ringtoneStart = ringtoneStart;
    }

    public float getRingtoneEnd() {
        return ringtoneEnd;
    }

    public void setRingtoneEnd(float ringtoneEnd) {
        this.ringtoneEnd = ringtoneEnd;
    }

    public long getUid() {
        return uid;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTime() {
        return time;
    }

    public String getLabel() {
        return label;
    }

    public String getRepeatMode() {
        return repeatMode;
    }

    public String getRingtoneUri() {
        return ringtoneUri;
    }

    public float getVolume() {
        return volume;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setRepeatMode(String repeatMode) {
        this.repeatMode = repeatMode;
    }

    public void setRingtoneUri(String ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public Alarm(boolean enabled, boolean selected, String time, String label, String repeatMode, String ringtoneUri, float ringtoneStart, float ringtoneEnd, float volume) {
        this.enabled = enabled;
        this.selected = selected;
        this.time = time;
        this.label = label;
        this.repeatMode = repeatMode;
        this.ringtoneUri = ringtoneUri;
        this.ringtoneStart = ringtoneStart;
        this.ringtoneEnd = ringtoneEnd;
        this.volume = volume;
    }
}