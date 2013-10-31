package com.tourist.RSSReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditChannelActivity extends Activity {

    String channelURL = null;
    String channelTitle = null;
    String channelTime = null;
    int channelID = 0;

    boolean add = false;

    EditText editTitle;
    EditText editURL;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_channel);
        Intent intent = getIntent();
        String editType = intent.getStringExtra("type");
        if ("add".equals(editType)) {
            add = true;
            Button button = (Button) findViewById(R.id.deleteButton);
            button.setText("Cancel");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteChannel();
                }
            });
        }
        channelURL = intent.getStringExtra("channelURL");
        channelTitle = intent.getStringExtra("channelTitle");
        channelTime = intent.getStringExtra("channelTime");
        channelID = Integer.parseInt(intent.getStringExtra("channelID"));
        editTitle = (EditText) findViewById(R.id.editTitle);
        editURL = (EditText) findViewById(R.id.editURL);
        editTitle.setText(channelTitle);
        editURL.setText(channelURL);
    }

    public void editChannel(View view) {
        channelTitle = editTitle.getText().toString();
        channelURL = editURL.getText().toString();
        DBAdapter myDBAdapter = new DBAdapter(this);
        myDBAdapter.open();
        if (add) {
            long channelID = myDBAdapter.createChannel(channelTitle, channelURL, "never");
            myDBAdapter.createChannelTable(channelID);
        } else {
            myDBAdapter.updateChannel(channelID, channelTitle, channelURL, channelTime);
        }
        myDBAdapter.close();
        this.finish();
    }

    public void deleteChannelAsk(View view) {
        deleteChannel();
    }

    public void deleteChannel() {
        if (!add) {
            DBAdapter myDBAdapter = new DBAdapter(this);
            myDBAdapter.open();
            myDBAdapter.deleteChannel(channelID);
            myDBAdapter.close();
        }
        this.finish();
    }
}