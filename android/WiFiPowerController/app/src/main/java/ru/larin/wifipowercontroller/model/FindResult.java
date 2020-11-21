package ru.larin.wifipowercontroller.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.larin.wifipowercontroller.lib.FindResponse;

public class FindResult implements Serializable{
    private List<FindResponse> findResponse = new ArrayList<FindResponse>();
    private Exception error;

    public List<FindResponse> getFindResponse() {
        return findResponse;
    }

    public void setFindResponse(List<FindResponse> findResponse) {
        this.findResponse = findResponse;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }
}
