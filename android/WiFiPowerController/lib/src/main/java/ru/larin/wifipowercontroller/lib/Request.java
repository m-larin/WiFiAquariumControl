package ru.larin.wifipowercontroller.lib;

/**
 * Created by larin on 06.06.2016.
 */
public class Request {
    private String command;
    private Integer channel;
    private Settings settings;

    public Request(String command){
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
