package ru.larin.wifipowercontroller.lib;

import java.io.Serializable;

/**
 * Created by mihail on 12.06.2016.
 */
public class Lighting implements Serializable{
    private int on;
    private int off;
    private int channel;
    private boolean enable;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getOn() {
        return on;
    }

    public void setOn(int on) {
        this.on = on;
    }

    public int getOff() {
        return off;
    }

    public void setOff(int off) {
        this.off = off;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}
