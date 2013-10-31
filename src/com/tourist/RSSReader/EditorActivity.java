package com.tourist.RSSReader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class EditorActivity extends Activity {

    final DBAdapter myDBAdapter = new DBAdapter(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);
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
                Intent intent = new Intent(view.getContext(), EditChannelActivity.class);
                cursor.moveToPosition(position);
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TITLE));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_URL));
                int ID = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROW_ID)));
                intent.putExtra("channelURL", url);
                intent.putExtra("channelTitle", title);
                intent.putExtra("channelID", "" + ID);
                intent.putExtra("type", "edit");
                startActivity(intent);
            }
        });
    }

    public void addChannel(View view) {
        long channelID = myDBAdapter.createChannel("", "");
        myDBAdapter.createChannelTable(channelID);
        Intent intent = new Intent(view.getContext(), EditChannelActivity.class);
        intent.putExtra("channelURL", "");
        intent.putExtra("channelTitle", "");
        intent.putExtra("channelID", "" + channelID);
        intent.putExtra("type", "add");
        startActivity(intent);
    }
}