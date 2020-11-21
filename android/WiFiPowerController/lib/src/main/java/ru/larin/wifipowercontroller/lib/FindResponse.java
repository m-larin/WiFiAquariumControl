package ru.larin.wifipowercontroller.lib;

import java.io.Serializable;

public class FindResponse implements Serializable {
    private Long id;
    private String ip;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "id: " + id + ", ip: '" + ip;
    }
}
