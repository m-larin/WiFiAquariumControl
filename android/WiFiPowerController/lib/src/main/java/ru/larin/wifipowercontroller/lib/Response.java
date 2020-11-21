package ru.larin.wifipowercontroller.lib;

import java.io.Serializable;

/**
 * Created by larin on 06.06.2016.
 */
public class Response implements Serializable {
    private String error;

    private boolean connectError;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isConnectError() {
        return connectError;
    }

    public void setConnectError(boolean connectError) {
        this.connectError = connectError;
    }
}
