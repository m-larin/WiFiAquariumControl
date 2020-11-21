package ru.larin.wifipowercontroller.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;

import java.util.Calendar;

import ru.larin.wifipowercontroller.R;
import ru.larin.wifipowercontroller.model.ChannelSettings;
import ru.larin.wifipowercontroller.lib.ScheduleSetting;

public class SettingsItemActivity extends AppCompatActivity {
    private ScheduleSetting settings;
    private TimePicker timePicker;
    private Switch switchCommand;
    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;
    private CheckBox checkBox5;
    private CheckBox checkBox6;
    private CheckBox checkBox7;
    private Spinner spinnerChannel;
    private ChannelSettings[] channelInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = (ScheduleSetting)getIntent().getSerializableExtra("item");
        channelInfos = (ChannelSettings[])getIntent().getSerializableExtra(WifiPowerController.CHANNEL_SETTINGS);

        setContentView(R.layout.activity_settings_item);

        timePicker = (TimePicker)findViewById(R.id.timePicker);
        switchCommand = (Switch)findViewById(R.id.switchCommand);
        checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
        checkBox2 = (CheckBox) findViewById(R.id.checkBox2);
        checkBox3 = (CheckBox) findViewById(R.id.checkBox3);
        checkBox4 = (CheckBox) findViewById(R.id.checkBox4);
        checkBox5 = (CheckBox) findViewById(R.id.checkBox5);
        checkBox6 = (CheckBox) findViewById(R.id.checkBox6);
        checkBox7 = (CheckBox) findViewById(R.id.checkBox7);
        spinnerChannel = (Spinner) findViewById(R.id.spinnerChannel);

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        for (int i=0; i<channelInfos.length; i++){
            adapter.add(getResources().getString(channelInfos[i].getChannelName()));
        }
        spinnerChannel.setAdapter(adapter);

        timePicker.setIs24HourView(true);

        //Инициализация
        if (settings.getCommand() == null){
            Calendar now = Calendar.getInstance();
            timePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(now.get(Calendar.MINUTE));
        }else{
            //Вычисляем время из минут суток
            timePicker.setCurrentHour(settings.getTime() / 60);
            timePicker.setCurrentMinute(settings.getTime() % 60);

            switchCommand.setChecked(settings.getCommand().equals("on"));

            checkBox1.setChecked((settings.getDay() & 1) == 1);
            checkBox2.setChecked((settings.getDay() & 2) == 2);
            checkBox3.setChecked((settings.getDay() & 4) == 4);
            checkBox4.setChecked((settings.getDay() & 8) == 8);
            checkBox5.setChecked((settings.getDay() & 16) == 16);
            checkBox6.setChecked((settings.getDay() & 32) == 32);
            checkBox7.setChecked((settings.getDay() & 64) == 64);

            spinnerChannel.setSelection(settings.getChannel());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_config_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();

        //Сохранение
        settings.setTime(timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute());
        settings.setChannel(spinnerChannel.getSelectedItemPosition());
        settings.setCommand(switchCommand.isChecked() ? "on" : "off");

        int day = checkBox1.isChecked() ? 1 : 0;
        day |= checkBox2.isChecked() ? 2 : 0;
        day |= checkBox3.isChecked() ? 4 : 0;
        day |= checkBox4.isChecked() ? 8 : 0;
        day |= checkBox5.isChecked() ? 16 : 0;
        day |= checkBox6.isChecked() ? 32 : 0;
        day |= checkBox7.isChecked() ? 64 : 0;

        settings.setDay(day);

        intent.putExtra("item", settings);
        setResult(RESULT_OK, intent);
        finish();
        return super.onOptionsItemSelected(item);
    }
}
