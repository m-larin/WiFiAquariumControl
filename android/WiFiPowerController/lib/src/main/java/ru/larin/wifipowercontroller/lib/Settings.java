package ru.larin.wifipowercontroller.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by larin on 06.06.2016.
 */
public class Settings extends Response{
    private List<ScheduleSetting> schedules = new ArrayList<ScheduleSetting>();
    private Lighting lighting;

    public List<ScheduleSetting> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleSetting> schedules) {
        this.schedules = schedules;
    }

    public Lighting getLighting() {
        return lighting;
    }

    public void setLighting(Lighting lighting) {
        this.lighting = lighting;
    }
}
