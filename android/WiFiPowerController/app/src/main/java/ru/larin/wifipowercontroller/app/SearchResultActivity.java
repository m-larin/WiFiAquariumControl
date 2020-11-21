package ru.larin.wifipowercontroller.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ru.larin.wifipowercontroller.R;
import ru.larin.wifipowercontroller.data.WpcDatabaseHelper;
import ru.larin.wifipowercontroller.lib.FindResponse;
import ru.larin.wifipowercontroller.model.Device;
import ru.larin.wifipowercontroller.model.FindResult;

public class SearchResultActivity extends AppCompatActivity {

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            WpcDatabaseHelper dataHelper = new WpcDatabaseHelper(SearchResultActivity.this);

            FindResponse item = (FindResponse) parent.getItemAtPosition(position);
            Device devise = new Device();
            devise.setId(item.getId());
            devise.setIp(item.getIp());
            devise.setName("WPC-" + item.getId());
            dataHelper.saveDevice(devise);

            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private ListView listView;
    private List<FindResponse> viewData = new ArrayList<FindResponse>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        FindResult findResult = (FindResult) getIntent().getSerializableExtra(WifiPowerController.FIND_RESULT);

        listView = (ListView) findViewById(R.id.listView);

        ArrayAdapter<FindResponse> adapter = new ArrayAdapter<FindResponse>(this, android.R.layout.simple_list_item_1, viewData);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);

        fillListView(findResult);
    }

    private void fillListView(FindResult findResult) {
        viewData.clear();
        for (FindResponse findResponse : findResult.getFindResponse()) {
            viewData.add(findResponse);
        }
        ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

}
