package com.tourist.RSSReader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ChooserActivity extends Activity {

    final DBAdapter myDBAdapter = new DBAdapter(this);

    MyBroadcastReceiver mbr = null;
    IntentFilter intentFilter;
    boolean registered = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooser);
        myDBAdapter.open();
        final Cursor cursor = myDBAdapter.fetchAllChannels();
        startManagingCursor(cursor);
        String[] from = new String[]{DBAdapter.KEY_C_TITLE};
        int[] to = new int[]{R.id.chooser_row_text};
        SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.chooser_row, cursor, from, to);
        ListView lvChooser = (ListView) findViewById(R.id.lvChooser);
        lvChooser.setAdapter(notes);
        lvChooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), MyActivity.class);
                cursor.moveToPosition(position);
                String channelURL = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_URL));
                String channelTitle = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TITLE));
                String channelTime = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TIME));
                int channelID = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROW_ID)));
                intent.putExtra("channelURL", channelURL);
                intent.putExtra("channelTitle", channelTitle);
                intent.putExtra("channelTime", channelTime);
                intent.putExtra("channelID", "" + channelID);
                startActivity(intent);
            }
        });
        mbr = new MyBroadcastReceiver();
        intentFilter = new IntentFilter(MyIntentService.key);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mbr, intentFilter);
        registered = true;
    }

    public void editList(View view) {
        Intent intent = new Intent(view.getContext(), EditorActivity.class);
        startActivity(intent);
    }

    public void updateAll(View view) {
        Toast toast = Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT);
        toast.show();
        Cursor cursor = myDBAdapter.fetchAllChannels();
        startManagingCursor(cursor);
        if (cursor.moveToFirst()) {
            do {
                Intent newIntent = new Intent(this, MyIntentService.class);
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TITLE));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_URL));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TIME));
                int ID = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROW_ID)));
                newIntent.putExtra("channelTitle", title);
                newIntent.putExtra("channelTime", time);
                newIntent.putExtra("channelURL", url);
                newIntent.putExtra("channelID", "" + ID);
                startService(newIntent);
            } while (cursor.moveToNext());
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("result");
            String title = intent.getStringExtra("channelTitle");
            if ("good".equals(result)) {
                showGoodLuck("Channel \"" + title + "\"");
            } else {
                showBadLuck("Channel \"" + title + "\"");
            }
        }
    }

    private static final String BAD_LUCK = "illegal feed URL or no Internet connection!";

    public void showBadLuck(String title) {
        Toast toast = Toast.makeText(this, title + ": " + BAD_LUCK, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void showGoodLuck(String title) {
        Toast toast = Toast.makeText(this, title + " updated", Toast.LENGTH_SHORT);
        toast.show();
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

}
