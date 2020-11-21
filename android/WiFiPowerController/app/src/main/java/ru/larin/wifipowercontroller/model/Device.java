package ru.larin.wifipowercontroller.model;

import java.io.Serializable;

public class Device implements Serializable{
    private long id;
    private String ip;
    private String name;
    private byte[] img;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }

    @Override
    public String toString(){
        return name + " (" + ip + ")";
    }

}
