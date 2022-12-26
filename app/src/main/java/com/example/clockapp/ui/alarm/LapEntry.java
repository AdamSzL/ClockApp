package com.example.clockapp.ui.alarm;

public class LapEntry {
    private int id;
    private long lapTime;
    private long totalTime;
    private boolean isShortest;
    private boolean isLongest;

    public LapEntry(int id, long lapTime, long totalTime) {
        this.id = id;
        this.lapTime = lapTime;
        this.totalTime = totalTime;
        this.isShortest = false;
        this.isLongest = false;
    }

    public boolean isShortest() {
        return isShortest;
    }

    public void setShortest(boolean shortest) {
        isShortest = shortest;
    }

    public boolean isLongest() {
        return isLongest;
    }

    public void setLongest(boolean longest) {
        isLongest = longest;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getLapTime() {
        return lapTime;
    }

    public void setLapTime(long lapTime) {
        this.lapTime = lapTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }
}
