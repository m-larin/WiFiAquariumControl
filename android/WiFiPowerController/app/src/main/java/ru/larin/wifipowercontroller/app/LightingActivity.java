package ru.larin.wifipowercontroller.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import ru.larin.wifipowercontroller.R;
import ru.larin.wifipowercontroller.lib.Lighting;
import ru.larin.wifipowercontroller.lib.Settings;
import ru.larin.wifipowercontroller.model.ChannelSettings;

public class LightingActivity extends AppCompatActivity {
    private Settings settings;
    private ChannelSettings[] channelInfos;
    private Spinner spinnerChannel;
    private SeekBar seekBarOff;
    private SeekBar seekBarOn;
    private Switch switchLightingControlEnable;
    private TextView textViewBarOn;
    private TextView textViewBarOff;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting);

        settings = (Settings) getIntent().getSerializableExtra(WifiPowerController.SCHEDULE_SETTINGS);
        channelInfos = (ChannelSettings[])getIntent().getSerializableExtra(WifiPowerController.CHANNEL_SETTINGS);

        seekBarOff = (SeekBar) findViewById(R.id.seekBarOff);
        seekBarOn = (SeekBar) findViewById(R.id.seekBarOn);
        spinnerChannel = (Spinner) findViewById(R.id.spinnerChannel);
        switchLightingControlEnable = (Switch)findViewById(R.id.switchLightingControlEnable);
        textViewBarOn = (TextView) findViewById(R.id.textViewBarOn);
        textViewBarOff = (TextView) findViewById(R.id.textViewBarOff);

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        for (int i=0; i<channelInfos.length; i++){
            adapter.add(getResources().getString(channelInfos[i].getChannelName()));
        }
        spinnerChannel.setAdapter(adapter);
        if (settings.getLighting() != null) {
            spinnerChannel.setSelection(settings.getLighting().getChannel());
            seekBarOn.setProgress(settings.getLighting().getOn());
            seekBarOff.setProgress(settings.getLighting().getOff());
            switchLightingControlEnable.setChecked(settings.getLighting().isEnable());
            textViewBarOn.setText(String.valueOf(settings.getLighting().getOn()));
            textViewBarOff.setText(String.valueOf(settings.getLighting().getOff()));
        }
        seekBarOn.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewBarOn.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewBarOn.setText(String.valueOf(seekBar.getProgress()));
            }
        });

        seekBarOff.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewBarOff.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewBarOff.setText(String.valueOf(seekBar.getProgress()));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_config_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (settings.getLighting() == null){
            settings.setLighting(new Lighting());
        }
        settings.getLighting().setChannel(spinnerChannel.getSelectedItemPosition());
        settings.getLighting().setOn(seekBarOn.getProgress());
        settings.getLighting().setOff(seekBarOff.getProgress());
        settings.getLighting().setEnable(switchLightingControlEnable.isChecked());

        Intent intent = new Intent();
        intent.putExtra("settings", settings);
        setResult(RESULT_OK, intent);
        finish();
        return super.onOptionsItemSelected(item);
    }

}
