package ru.larin.wifipowercontroller.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import ru.larin.wifipowercontroller.R;
import ru.larin.wifipowercontroller.data.WpcDatabaseHelper;
import ru.larin.wifipowercontroller.lib.FindResponse;
import ru.larin.wifipowercontroller.model.ChannelSettings;
import ru.larin.wifipowercontroller.lib.Request;
import ru.larin.wifipowercontroller.lib.Response;
import ru.larin.wifipowercontroller.lib.Settings;
import ru.larin.wifipowercontroller.lib.Status;
import ru.larin.wifipowercontroller.model.Device;
import ru.larin.wifipowercontroller.model.FindResult;
import ru.larin.wifipowercontroller.service.FindServiceTask;

/*
    exec("{\"command\":\"on\",\"channel\":0}");
    exec("{\"command\":\"off\",\"channel\":0}");
    exec("{\"command\":\"on\",\"channel\":1}");
    exec("{\"command\":\"off\",\"channel\":1}");
    exec("{\"command\":\"on\",\"channel\":2}");
    exec("{\"command\":\"off\",\"channel\":2}");
    exec("{\"command\":\"on\",\"channel\":3}");
    exec("{\"command\":\"off\",\"channel\":3}");
    exec("{\"command\":\"settings-set\",\"settings\":{\"schedules\":[{\"time\":1402, \"channel\":0, \"command\":\"on\"}]}}");
    exec("{\"command\":\"settings-get\"}");
    exec("{\"command\":\"get-status\"}");

    {"lighting":65535,"time":{"min":37,"sec":6,"hour":22},"channels":{"1":"off","2":"off","3":"off","0":"on"}}
 */

public class ControllerActivity extends AppCompatActivity {
    private final String TAG = "ControllerActivity";

    private String ip;
    private long id;

    private ChannelSettings[] channelInfos;
    private Status status;
    private ImageButton[] buttons = new ImageButton[4];
    private LinearLayout statusLayout;
    private TextView textViewTime;
    private TextView textViewLighting;
    private Timer timer;
    private LinearLayout connectErrorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        buttons[0] = (ImageButton)findViewById(R.id.imageButton0);
        buttons[1] = (ImageButton)findViewById(R.id.imageButton1);
        buttons[2] = (ImageButton)findViewById(R.id.imageButton2);
        buttons[3] = (ImageButton)findViewById(R.id.imageButton3);

        statusLayout = (LinearLayout)findViewById(R.id.statusLayout);
        connectErrorLayout= (LinearLayout)findViewById(R.id.connectStatusLayout);

        channelInfos = (ChannelSettings[])getIntent().getSerializableExtra(WifiPowerController.CHANNEL_SETTINGS);
        ip = getIntent().getStringExtra(WifiPowerController.IP_PARAM);
        id = getIntent().getLongExtra(WifiPowerController.ID_PARAM, 0);

        textViewTime = (TextView)findViewById(R.id.textViewTime);
        textViewLighting = (TextView)findViewById(R.id.textViewLighting);

        updateStatus();
    }

    private void updateStatus() {
        try {
            Request command = new Request("get-status");
            executeCommand(command, Status.class, new OnReceiveStatus());
        } catch (Exception ex) {
            ErrorDialog.showError(ControllerActivity.this,"Error", ex);
        }
    }

    class OnReceiveStatus implements OnPostExecute {

        @Override
        public void onPostExecute(Response result) {
            try {
                if (result.getError() == null) {
                    status = (Status) result;
                    for (int i = 0; i < channelInfos.length; i++) {
                        ImageButton button = buttons[channelInfos[i].getChannelNum()];
                        if (status.getChannels().get(String.valueOf(channelInfos[i].getChannelNum())).equals("on")) {
                            button.setImageResource(channelInfos[i].getImage());
                        } else {
                            button.setImageResource(channelInfos[i].getImageGray());
                        }
                    }

                    if (status.isHasUpdate()){
                        statusLayout.setVisibility(View.VISIBLE);
                    }else{
                        statusLayout.setVisibility(View.GONE);
                    }

                    textViewTime.setText(status.getTime().getHour() + ":" + status.getTime().getMin() + ":" + status.getTime().getSec());
                    textViewLighting.setText(String.valueOf(status.getLighting()));

                    connectErrorLayout.setVisibility(View.GONE);
                } else {
                    if (result.isConnectError()){
                        connectErrorLayout.setVisibility(View.VISIBLE);
                        //Обновить информациюоб цстройствах
                        //checkDevices();
                    }else {
                        connectErrorLayout.setVisibility(View.GONE);
                        throw new Exception(result.getError());
                    }
                }
            } catch (Exception ex) {
                ErrorDialog.showError(ControllerActivity.this,"Error", ex);
            }
        }
    }

    private void executeCommand(Request command, final Class<? extends Response> responceType, final OnPostExecute onPostExecute) {
        new AsyncTask<Request, Void, Response>() {

            @Override
            protected Response doInBackground(Request ... params) {
                Socket socket = null;
                try {
                    socket = new Socket(ip, 7456);
                    Request command = params[0];

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    String json = gson.toJson(command);
                    Log.i(TAG, "send: " + json);
                    socket.getOutputStream().write(json.getBytes());
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while ((read = socket.getInputStream().read(buffer)) > 0){
                        bs.write(buffer, 0, read);
                    }
                    String result = new String(bs.toByteArray());
                    Log.i(TAG, "receive: " + result);
                    gson = builder.create();
                    return gson.fromJson(result, responceType);
                } catch (ConnectException ex) {
                    Response error = new Response();
                    error.setConnectError(true);
                    error.setError(ex.toString());
                    return error;
                } catch (Exception ex) {
                    Response error = new Response();
                    error.setError(ex.toString());
                    return error;
                } finally {
                    try {
                        if (socket != null && !socket.isClosed()) {
                            socket.close();
                        }
                    } catch (Exception ignoreEx) {
                    }
                }
            }

            @Override
            protected void onPostExecute(Response result) {
                super.onPostExecute(result);
                onPostExecute.onPostExecute(result);
            }
        }.execute(command);
    }

    private void checkDevices() {
        new FindServiceTask(){

            @Override
            protected boolean onFindDevice(FindResponse deviceInfo) {
                WpcDatabaseHelper helper = new WpcDatabaseHelper(ControllerActivity.this);
                Device device = helper.findDevice(id);
                if (!device.getIp().equals(ip)){
                    device.setIp(deviceInfo.getIp());
                    helper.saveDevice(device);
                    Log.i(TAG, "Update ip on device " + device.getId() + " for " + deviceInfo.getIp());
                    ip = deviceInfo.getIp();
                }
                return true;
            }

            @Override
            protected void onEndFindDevice(FindResult findResult) {
            }
        }.executeSync();
    }

    public void onClick(View view) {
        try {

            updateStatus();

            String tag = (String) view.getTag();
            int channel = Integer.parseInt(tag.substring(tag.length() - 1));

            String commandString = status.getChannels().get(String.valueOf(channel)).equals("on") ? "off" : "on";
            Request command = new Request(commandString);
            command.setChannel(channel);
            executeCommand(command, Status.class, new OnReceiveStatus());

        } catch (Exception ex) {
            ErrorDialog.showError(this,"Error", ex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        try{
            Request command = new Request("settings-get");
            executeCommand(command, Settings.class, new OnPostExecute(){

                @Override
                public void onPostExecute(Response result) {
                    Intent intent = null;
                    if (item.getItemId() == R.id.controller_schedule) {
                        intent = new Intent(ControllerActivity.this, SettingsActivity.class);
                    }else{
                        intent = new Intent(ControllerActivity.this, LightingActivity.class);
                    }
                    intent.putExtra(WifiPowerController.SCHEDULE_SETTINGS, (Settings)result);
                    intent.putExtra(WifiPowerController.CHANNEL_SETTINGS, channelInfos);
                    startActivityForResult(intent, 0);
                }
            });

            return super.onOptionsItemSelected(item);
        } catch (Exception ex) {
            ErrorDialog.showError(this,"Error", ex);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK){
            Settings settings =  (Settings)data.getSerializableExtra("settings");

            Request command = new Request("settings-set");
            command.setSettings(settings);
            executeCommand(command, Status.class, new OnReceiveStatus());
        }
    }

    public boolean onUpdate(View viwe){
        Request command = new Request("update-firmware");
        executeCommand(command, Status.class, new OnReceiveStatus());
        statusLayout.setVisibility(View.GONE);
        return true;
    }


    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus){
            if (timer == null) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateStatus();
                    }
                }, 2000, 2000);
            }
        }else{
            if (timer != null){
                timer.cancel();
                timer.purge();
                timer = null;
            }
        }

    }
}
