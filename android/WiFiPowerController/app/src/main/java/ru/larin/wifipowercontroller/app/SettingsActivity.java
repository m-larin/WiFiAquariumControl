package ru.larin.wifipowercontroller.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.larin.wifipowercontroller.R;
import ru.larin.wifipowercontroller.model.ChannelSettings;
import ru.larin.wifipowercontroller.lib.ScheduleSetting;
import ru.larin.wifipowercontroller.lib.Settings;

public class SettingsActivity extends AppCompatActivity {
    public static final int MENU_CONTEXT_DELETE_ID = 0;

    private Settings settings;
    private ListView listView;
    private List<Map<String, Object>> viewData = new ArrayList<Map<String, Object>>();
    private ChannelSettings[] channelInfos;

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map item = (Map) parent.getItemAtPosition(position);
            ScheduleSetting settings = getScheduleSettingById((int) item.get("id"));
            Intent intent = new Intent(SettingsActivity.this, SettingsItemActivity.class);
            intent.putExtra("item", settings);
            intent.putExtra(WifiPowerController.CHANNEL_SETTINGS, channelInfos);
            startActivityForResult(intent, 1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = (Settings) getIntent().getSerializableExtra(WifiPowerController.SCHEDULE_SETTINGS);
        channelInfos = (ChannelSettings[]) getIntent().getSerializableExtra(WifiPowerController.CHANNEL_SETTINGS);

        setContentView(R.layout.activity_settings);

        listView = (ListView) findViewById(R.id.listView);


        SimpleAdapter adapter = new SimpleAdapter(this, viewData, android.R.layout.simple_list_item_2,
                new String[]{"name", "description"},
                new int[]{android.R.id.text1, android.R.id.text2});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);

        registerForContextMenu(listView);

        fillListView();
    }

    private void fillListView() {
        viewData.clear();
        for (ScheduleSetting scheduleSetting : settings.getSchedules()) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("id", scheduleSetting.getId());
            row.put("name", getResources().getString(channelInfos[scheduleSetting.getChannel()].getChannelName()) + " " + getCommand(scheduleSetting.getCommand()));
            row.put("description", getTime(scheduleSetting.getTime()) + " " + getDays(scheduleSetting.getDay()));
            viewData.add(row);
        }
        ((SimpleAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    private String getCommand(String command) {
        if (command.equals("on")) {
            return getResources().getString(R.string.on);
        } else {
            return getResources().getString(R.string.off);
        }
    }

    private String getTime(int time) {
        int hour = time / 60;
        int min = time % 60;
        return (hour < 10 ? "0" : "") + hour + ":" + (min < 10 ? "0" : "") + min;
    }

    private String getDays(int day) {
        String result = "";
        if (day == 0) {
            result = getResources().getString(R.string.never);
        } else if (day == 127) {
            result = getResources().getString(R.string.every_day);
        } else {
            if ((day & 1) == 1) {
                result += getResources().getString(R.string.monday) + ",";
            }
            if ((day & 2) == 2) {
                result += getResources().getString(R.string.tuesday) + ",";
            }
            if ((day & 4) == 4) {
                result += getResources().getString(R.string.wednesday) + ",";
            }
            if ((day & 8) == 8) {
                result += getResources().getString(R.string.thursday) + ",";
            }
            if ((day & 16) == 16) {
                result += getResources().getString(R.string.friday) + ",";
            }
            if ((day & 32) == 32) {
                result += getResources().getString(R.string.saturday) + ",";
            }
            if ((day & 64) == 64) {
                result += getResources().getString(R.string.sunday) + ",";
            }
        }
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public void onAddClick(View view) {
        Intent intent = new Intent(this, SettingsItemActivity.class);
        intent.putExtra("item", new ScheduleSetting(getMaxId() + 1));
        intent.putExtra(WifiPowerController.CHANNEL_SETTINGS, channelInfos);
        startActivityForResult(intent, 0);
    }

    private int getMaxId() {
        int result = 0;
        for (ScheduleSetting scheduleSetting : settings.getSchedules()) {
            if (scheduleSetting.getId() > result) {
                result = scheduleSetting.getId();
            }
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                ScheduleSetting scheduleSetting = (ScheduleSetting) data.getSerializableExtra("item");
                settings.getSchedules().add(scheduleSetting);
                fillListView();
            } else if (requestCode == 1) {
                ScheduleSetting scheduleSetting = (ScheduleSetting) data.getSerializableExtra("item");
                ScheduleSetting item = getScheduleSettingById(scheduleSetting.getId());
                item.setCommand(scheduleSetting.getCommand());
                item.setDay(scheduleSetting.getDay());
                item.setChannel(scheduleSetting.getChannel());
                item.setTime(scheduleSetting.getTime());
                fillListView();
            }
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
        intent.putExtra("settings", settings);
        setResult(RESULT_OK, intent);
        finish();
        return super.onOptionsItemSelected(item);
    }

    private ScheduleSetting getScheduleSettingById(int id) {
        for (ScheduleSetting item : settings.getSchedules()) {
            if (id == item.getId()) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Map item = (Map) ((ListView) v).getAdapter().getItem(info.position);
        String title = item.get("name") + " " + item.get("description");
        menu.setHeaderTitle(title);

        menu.add(Menu.NONE, MENU_CONTEXT_DELETE_ID, Menu.NONE, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CONTEXT_DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Map row = (Map) (listView.getAdapter()).getItem(info.position);
                ScheduleSetting itemSettings = getScheduleSettingById((int) row.get("id"));
                for (ScheduleSetting scheduleSetting : settings.getSchedules()) {
                    if (scheduleSetting.getId() == itemSettings.getId()) {
                        settings.getSchedules().remove(scheduleSetting);
                        break;
                    }
                }
                fillListView();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
