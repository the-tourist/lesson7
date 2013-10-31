package com.tourist.RSSReader;
/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {

    public static final String KEY_C_TITLE = "title";
    public static final String KEY_C_URL = "url";
    public static final String KEY_C_TIME = "cTime";
    public static final String KEY_CHANNEL = "channel";
    public static final String KEY_A_SUMMARY = "summary";
    public static final String KEY_A_LINK = "link";
    public static final String KEY_A_TITLE = "title";
    public static final String KEY_ROW_ID = "_id";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String CHANNELS_TABLE = "channels";
    private static final String DATABASE_NAME = "touristDatabase3";
    private static final int DATABASE_VERSION = 1;

    private static final String INIT_CHANNELS =
            "create table " + CHANNELS_TABLE + " (" + KEY_ROW_ID + " integer primary key autoincrement, "
                            + KEY_C_TITLE + " text not null, "
                            + KEY_C_URL + " text not null, "
                            + KEY_C_TIME + " text not null)";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public static final String[] channelTitles = {"Stack Overflow: Android",
                                                      "Lenta.ru: Новости",
                                                      "Bash.im",
                                                      "IT happens",
                                                      "Они задолбали!",
                                                      "Хабрахабр",
                                                      "BBC News - Europe"};
        public static final String[] channelURLs = {"http://stackoverflow.com/feeds/tag/android",
                                                    "http://lenta.ru/rss",
                                                    "http://bash.im/rss",
                                                    "http://ithappens.ru/rss",
                                                    "http://zadolba.li/rss",
                                                    "http://habrahabr.ru/rss/hubs/",
                                                    "http://feeds.bbci.co.uk/news/world/europe/rss.xml"};

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(INIT_CHANNELS);
            for (int i = 0; i < channelTitles.length; i++) {
                ContentValues initialValues = new ContentValues();
                initialValues.put(KEY_C_TITLE, channelTitles[i]);
                initialValues.put(KEY_C_URL, channelURLs[i]);
                initialValues.put(KEY_C_TIME, "never");
                db.insert(CHANNELS_TABLE, null, initialValues);
                db.execSQL("create table " + KEY_CHANNEL + (i + 1) + " (" + KEY_ROW_ID + " integer primary key autoincrement, "
                                + KEY_A_SUMMARY + " text not null, "
                                + KEY_A_LINK + " text not null, "
                                + KEY_A_TITLE + " text not null)");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + CHANNELS_TABLE);
            onCreate(db);
        }
    }

    public DBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDb.close();
        mDbHelper.close();
    }

    void createChannelTable(long channelID) {
        mDb.execSQL("create table " + KEY_CHANNEL + (channelID) + " (" + KEY_ROW_ID + " integer primary key autoincrement, "
                        + KEY_A_SUMMARY + " text not null, "
                        + KEY_A_LINK + " text not null, "
                        + KEY_A_TITLE + " text not null)");
    }

    void dropChannelTable(long channelID) {
        mDb.execSQL("drop table if exists " + KEY_CHANNEL + (channelID));
    }

    void clearChannelTable(long channelID) {
        dropChannelTable(channelID);
        createChannelTable(channelID);
    }

    public long addNews(int channelID, String summary, String link, String title) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_A_SUMMARY, summary);
        initialValues.put(KEY_A_LINK, link);
        initialValues.put(KEY_A_TITLE, title);
        return mDb.insert(KEY_CHANNEL + channelID, null, initialValues);
    }

    public long createChannel(String title, String url) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_C_TITLE, title);
        initialValues.put(KEY_C_URL, url);
        return mDb.insert(CHANNELS_TABLE, null, initialValues);
    }

    public boolean deleteChannel(long rowId) {
        return mDb.delete(CHANNELS_TABLE, KEY_ROW_ID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllChannels() {
        return mDb.query(CHANNELS_TABLE, new String[] {KEY_ROW_ID, KEY_C_TITLE,
                KEY_C_URL, KEY_C_TIME}, null, null, null, null, null);
    }

    public Cursor fetchAllArticles(int channelID) {

        return mDb.query(KEY_CHANNEL + channelID, new String[] {KEY_ROW_ID, KEY_A_TITLE,
                KEY_A_SUMMARY, KEY_A_LINK}, null, null, null, null, null);
    }

    public boolean updateChannel(long rowId, String title, String url) {
        ContentValues args = new ContentValues();
        args.put(KEY_C_TITLE, title);
        args.put(KEY_C_URL, url);
        return mDb.update(CHANNELS_TABLE, args, KEY_ROW_ID + "=" + rowId, null) > 0;
    }

    public boolean updateChannelTime(long rowId, String cTime) {
        ContentValues args = new ContentValues();
        args.put(KEY_C_TIME, cTime);
        return mDb.update(CHANNELS_TABLE, args, KEY_ROW_ID + "=" + rowId, null) > 0;
    }
}
