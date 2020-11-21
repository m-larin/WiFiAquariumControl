package ru.larin.wifipowercontroller.lib;

import java.io.Serializable;

public class WpcTime implements Serializable{
    private int sec;
    private int min;
    private int hour;
    private int day;
    private int date;
    private int month;
    private int year;

    public WpcTime() {
    }

    public WpcTime(int year, int month, int date, int hour, int min, int sec) {
        this.sec = sec;
        this.min = min;
        this.hour = hour;
        this.date = date;
        this.month = month;
        this.year = year;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
