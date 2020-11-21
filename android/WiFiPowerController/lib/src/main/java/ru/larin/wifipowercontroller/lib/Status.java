package ru.larin.wifipowercontroller.lib;

import java.util.Map;

/**
 * Created by larin on 06.06.2016.
 */
public class Status extends Response {
    private Map<String, String> channels;
    private int lighting;
    private WpcTime time;

    public boolean isHasUpdate() {
        return hasUpdate;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    private boolean hasUpdate;

    public Map<String, String> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, String> channels) {
        this.channels = channels;
    }

    public int getLighting() {
        return lighting;
    }

    public void setLighting(int lighting) {
        this.lighting = lighting;
    }

    public WpcTime getTime() {
        return time;
    }

    public void setTime(WpcTime time) {
        this.time = time;
    }
}
