package com.tourist.RSSReader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

public class MyActivity extends Activity {

    String channelURL = null;
    String channelTime = null;
    int channelID = 0;

    ArrayList<String> summaries;
    ArrayList<String> links;
    ArrayList<String> titles;

    ListView lvMain;
    TextView tvMain;
    ArrayAdapter<String> adapter;

    MyBroadcastReceiver mbr = null;
    IntentFilter intentFilter;
    boolean registered = false;

    DBAdapter myDBAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent intent = getIntent();
        channelURL = intent.getStringExtra("channelURL");
        channelTime = intent.getStringExtra("channelTime");
        channelID = Integer.parseInt(intent.getStringExtra("channelID"));
        lvMain = (ListView) findViewById(R.id.lvMain);
        tvMain = (TextView) findViewById(R.id.tvMain);
        tvMain.setText("Latest update: " + channelTime);
        myDBAdapter = new DBAdapter(this);
        myDBAdapter.open();
        showFeed();
    }

    public void showFeed() {
        Cursor cursor = myDBAdapter.fetchAllArticles(channelID);
        summaries = new ArrayList<String>();
        links = new ArrayList<String>();
        titles = new ArrayList<String>();
        if (cursor.moveToFirst()) {
            tvMain.setText("Latest update: " + channelTime);
            do {
                if (!cursor.moveToNext()) {
                    break;
                }
                cursor.moveToPrevious();
                summaries.add(cursor.getString(cursor.getColumnIndexOrThrow("summary")));
                links.add(cursor.getString(cursor.getColumnIndexOrThrow("link")));
                titles.add(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            } while (cursor.moveToNext());
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, titles);
            lvMain.setAdapter(adapter);
            lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    showRecord(position);
                }
            });
        }
    }

    public void updateFeed(View view) {
        Toast toast = Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT);
        toast.show();
        Intent newIntent = new Intent(this, MyIntentService.class);
        newIntent.putExtra("channelURL", channelURL);
        newIntent.putExtra("channelID", "" + channelID);
        startService(newIntent);
        mbr = new MyBroadcastReceiver();
        intentFilter = new IntentFilter(MyIntentService.key + channelID);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mbr, intentFilter);
        registered = true;
    }

    @Override
    protected void onPause() {
        if (mbr != null && registered) {
            unregisterReceiver(mbr);
            registered = false;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mbr != null && !registered) {
            registerReceiver(mbr, intentFilter);
            registered = true;
        }
    }

    @Override
    protected void onStop() {
        if (mbr != null && registered) {
            unregisterReceiver(mbr);
            registered = false;
        }
        myDBAdapter.close();
        super.onStop();
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("result");
            if ("good".equals(result)) {
                channelTime = intent.getStringExtra("cTime");
                showFeed();
            } else {
                showBadLuck();
            }
        }
    }

    private static final String BAD_LUCK = "Illegal feed URL or no Internet connection!";

    public void showBadLuck() {
        Toast toast = Toast.makeText(this, BAD_LUCK, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void showRecord(int position) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("summary", summaries.get(position));
        intent.putExtra("link", links.get(position));
        intent.putExtra("title", titles.get(position));
        startActivity(intent);
    }

}
