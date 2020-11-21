package ru.larin.wifipowercontroller.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.larin.wifipowercontroller.R;
import ru.larin.wifipowercontroller.data.WpcDatabaseHelper;
import ru.larin.wifipowercontroller.lib.FindResponse;
import ru.larin.wifipowercontroller.model.Device;
import ru.larin.wifipowercontroller.model.FindResult;
import ru.larin.wifipowercontroller.service.FindServiceTask;

public class DevisesActivity extends AppCompatActivity {
    public static final int MENU_CONTEXT_DELETE_ID = 0;
    private static final String TAG = DevisesActivity.class.getName();

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Device item = (Device) parent.getItemAtPosition(position);
            Intent intent = new Intent(DevisesActivity.this, DeviseActivity.class);
            intent.putExtra("device", item);
            startActivityForResult(intent, 1);
        }
    };
    private ProgressDialog progressDialog;
    private boolean cancelSearch = false;
    private android.widget.ListView listView;
    private List<Device> viewData = new java.util.ArrayList<Device>();
    private boolean changed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devises);
        listView = (android.widget.ListView) findViewById(R.id.listView);
        ArrayAdapter<Device> adapter = new ArrayAdapter<Device>(this, android.R.layout.simple_list_item_1, viewData);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);

        registerForContextMenu(listView);

        fillListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_devices, menu);
        return true;
    }

    public void onFind(MenuItem item) {

        new FindServiceTask(){

            @Override
            protected boolean onFindDevice(FindResponse deviceInfo) {
                WpcDatabaseHelper helper = new WpcDatabaseHelper(DevisesActivity.this);
                return !helper.exists(deviceInfo.getId());
            }

            @Override
            protected void onEndFindDevice(FindResult findResult) {
                if (!cancelSearch) {
                    progressDialog.dismiss();
                    if (findResult.getError() != null) {
                        ErrorDialog.showError(DevisesActivity.this, "Find error", findResult.getError());
                    } else {
                        if (findResult.getFindResponse().size() == 0) {
                            AlertDialog aDialog = new AlertDialog.Builder(DevisesActivity.this).setMessage(R.string.not_found).create();
                            aDialog.show();
                        } else {
                            Intent intent = new Intent(DevisesActivity.this, SearchResultActivity.class);
                            intent.putExtra(WifiPowerController.FIND_RESULT, findResult);
                            startActivityForResult(intent, 1);
                        }
                    }
                }
            }
        }.execute();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setMessage("Search WPC Devices ...");
        progressDialog.setTitle("Search");
        progressDialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                cancelSearch = true;
            }
        });
        progressDialog.show();

    }

    public void onAdd(MenuItem item) {
        Intent intent = new Intent(this, DeviseActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            fillListView();
            changed = true;
        }
    }

    private void fillListView() {
        viewData.clear();
        ru.larin.wifipowercontroller.data.WpcDatabaseHelper dataHelper = new ru.larin.wifipowercontroller.data.WpcDatabaseHelper(this);
        for (ru.larin.wifipowercontroller.model.Device devise : dataHelper.findAllDevice()) {
            viewData.add(devise);
        }
        ((android.widget.ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Device item = (Device) ((ListView) v).getAdapter().getItem(info.position);
        String title = item.getName() + " (" + item.getIp() + ")";
        menu.setHeaderTitle(title);

        menu.add(Menu.NONE, MENU_CONTEXT_DELETE_ID, Menu.NONE, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CONTEXT_DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Device row = (Device) (listView.getAdapter()).getItem(info.position);

                WpcDatabaseHelper dao = new WpcDatabaseHelper(this);
                dao.deleteDevice(row.getId());
                fillListView();
                changed = true;
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        if (changed) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }
}
