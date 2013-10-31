package com.tourist.RSSReader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ChooserActivity extends Activity {

    final DBAdapter myDBAdapter = new DBAdapter(this);

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
                String channelTime = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TIME));
                int channelID = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROW_ID)));
                intent.putExtra("channelURL", channelURL);
                intent.putExtra("channelTime", channelTime);
                intent.putExtra("channelID", "" + channelID);
                startActivity(intent);
            }
        });
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
                if (!cursor.moveToNext()) {
                    break;
                }
                cursor.moveToPrevious();
                Intent newIntent = new Intent(this, MyIntentService.class);
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_TITLE));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_C_URL));
                int ID = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROW_ID)));
                newIntent.putExtra("channelTitle", title);
                newIntent.putExtra("channelURL", url);
                newIntent.putExtra("channelID", "" + ID);
                startService(newIntent);
            } while (cursor.moveToNext());
        }
    }
}
