package ru.larin.wifipowercontroller.model;

import java.io.Serializable;

/**
 * Created by larin on 30.06.2016.
 */
public class ChannelSettings implements Serializable{
    private int image;
    private int imageGray;
    private int channelNum;
    private int channelName;

    public ChannelSettings(){
    }

    public ChannelSettings(int image, int imageGray, int channelNum, int channelName){
        this.image = image;
        this.imageGray = imageGray;
        this.channelNum = channelNum;
        this.channelName = channelName;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getImageGray() {
        return imageGray;
    }

    public void setImageGray(int imageGray) {
        this.imageGray = imageGray;
    }

    public int getChannelNum() {
        return channelNum;
    }

    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
    }

    public int getChannelName() {
        return channelName;
    }

    public void setChannelName(int channelName) {
        this.channelName = channelName;
    }
}
