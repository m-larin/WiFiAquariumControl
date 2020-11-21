package ru.larin.wifipowercontroller.lib;

import java.io.Serializable;

/**
 * Created by larin on 06.06.2016.
 */
public class ScheduleSetting implements Serializable {

    private int id;
    private int time;
    private int channel;
    private String command;
    private int day;

    public ScheduleSetting(){
    }

    public ScheduleSetting(int id){
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return time + " " + command + " " + channel + " " + day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
