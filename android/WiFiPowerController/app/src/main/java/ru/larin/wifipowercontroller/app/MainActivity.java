package ru.larin.wifipowercontroller.app;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.larin.wifipowercontroller.R;
import ru.larin.wifipowercontroller.data.WpcDatabaseHelper;
import ru.larin.wifipowercontroller.lib.ApkInfo;
import ru.larin.wifipowercontroller.lib.BuildInfo;
import ru.larin.wifipowercontroller.lib.FindResponse;
import ru.larin.wifipowercontroller.model.ChannelSettings;
import ru.larin.wifipowercontroller.model.Device;
import ru.larin.wifipowercontroller.model.FindResult;
import ru.larin.wifipowercontroller.service.FindServiceTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final String BASE_UPDATE_URL = "http://mlarin.no-ip.org:8080/wpc/";
    private Button updateButton;

    private class UpdateResult{
        private boolean hasUpdate;
        private Exception exception;
        private ApkInfo apkInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateButton = (Button)findViewById(R.id.buttonUpdateApk);

        checkUpdate();

        fillButtons();

        checkDevices();
    }

    private void checkDevices() {
        new FindServiceTask(){

            @Override
            protected boolean onFindDevice(FindResponse deviceInfo) {
                WpcDatabaseHelper helper = new WpcDatabaseHelper(MainActivity.this);
                if (helper.exists(deviceInfo.getId())){
                    Device device = helper.findDevice(deviceInfo.getId());
                    if (!device.getIp().equals(deviceInfo.getIp())){
                        device.setIp(deviceInfo.getIp());
                        helper.saveDevice(device);
                        Log.i(TAG, "Update ip on device " + device.getId() + " for " + deviceInfo.getIp());
                    }
                }
                return true;
            }

            @Override
            protected void onEndFindDevice(FindResult findResult) {
            }
        }.execute();
    }

    private void checkUpdate() {
        new AsyncTask<Void, Void, UpdateResult>() {

            @Override
            protected UpdateResult doInBackground(Void... voids) {
                UpdateResult result = new UpdateResult();
                try {
                    URL url = new URL("http://mlarin.no-ip.org:8080/wpc/output.json");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        Reader reader = new InputStreamReader(urlConnection.getInputStream());
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        BuildInfo[] buildsInfos = gson.fromJson(reader, BuildInfo[].class);
                        if (buildsInfos.length > 0){
                            int version = buildsInfos[0].getApkInfo().getVersionCode();
                            PackageInfo pInfo = MainActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
                            result.apkInfo = buildsInfos[0].getApkInfo();
                            if(pInfo.versionCode < version){
                                result.hasUpdate = true;
                            }
                        }
                    } finally {
                        urlConnection.disconnect();
                    }
                }catch(Exception ex){
                    result.exception = ex;
                }
                return result;
            }

            @Override
            protected void onPostExecute(final UpdateResult result) {
                super.onPostExecute(result);
                if (result.exception != null){
                    ErrorDialog.showError (MainActivity.this, "Error check update", result.exception);
                }else{
                    if (result.hasUpdate) {
                        updateButton.setVisibility(View.VISIBLE);
                        updateButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_UPDATE_URL + result.apkInfo.getOutputFile()));
                                startActivity(browserIntent);
                            }
                        });
                    }
                }
            }
        }.execute();
    }

    private void fillButtons(){
        LinearLayout mainLayout = (LinearLayout)findViewById(R.id.mainlayout);
        mainLayout.removeAllViews();

        final WpcDatabaseHelper helper = new WpcDatabaseHelper(this);
        for (Device device : helper.findAllDevice()){

            ImageButton button = new ImageButton(this);
            if (device.getImg() != null) {
                Bitmap image = BitmapFactory.decodeByteArray(device.getImg(), 0, device.getImg().length);
                button.setImageBitmap(image);
            }else{
                button.setImageResource(R.drawable.controller_button_0);
            }

            button.setBackgroundColor(Color.WHITE);
            button.setTag(device);
            button.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ControllerActivity.class);

                    //Получаем из базы на всякий случай, вдруг сменился ip
                    Device deviceInBase = helper.findDevice(((Device)view.getTag()).getId());
                    intent.putExtra(WifiPowerController.IP_PARAM, deviceInBase.getIp());
                    intent.putExtra(WifiPowerController.ID_PARAM, deviceInBase.getId());

                    ChannelSettings[] channelSettings = new ChannelSettings[4];
                    channelSettings[0] = new ChannelSettings(R.drawable.aeration, R.drawable.aeration_grey, 0, R.string.aeration);
                    channelSettings[1] = new ChannelSettings(R.drawable.filter, R.drawable.filter_gray, 1, R.string.filter);
                    channelSettings[2] = new ChannelSettings(R.drawable.heating, R.drawable.heating_gray, 2, R.string.heating);
                    channelSettings[3] = new ChannelSettings(R.drawable.illumination, R.drawable.illumination_gray, 3, R.string.illumination);
                    intent.putExtra(WifiPowerController.CHANNEL_SETTINGS, channelSettings);

                    startActivity(intent);
                }
            });
            mainLayout.addView(button);

            TextView text = new TextView(this);
            text.setText(device.getName());
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            mainLayout.addView(text);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, DevisesActivity.class);
        startActivityForResult(intent, 0);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            fillButtons();
        }
    }
}
