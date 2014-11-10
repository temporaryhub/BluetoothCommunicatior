package com.example.marcinbuczkowski.bluetoothchat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ChatMessageDatabase {
    private static ChatMessageDatabase instance = null;

    private SQLiteDatabase db;

    private final String DATABASE_NAME = "chat_messages";
    private final String TABLE_NAME = "chat_messages";

    private final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender TEXT NOT NULL," +
                    "receiver TEXT NOT NULL," +
                    "message TEXT NOT NULL);";

    protected ChatMessageDatabase(String path) {
        this.db = SQLiteDatabase.openOrCreateDatabase(path+"/"+this.DATABASE_NAME, null);
        try {
            this.db.execSQL(this.TABLE_CREATE);
        } catch (Exception e) { }
    }

    public static ChatMessageDatabase getInstance(String path) {
        if(instance == null) {
            instance = new ChatMessageDatabase(path);
        }
        return instance;
    }

    //todo adding to database and refreshing chat view
    public boolean save(String sender, String receiver, String message) {
        try {
            this.db.execSQL("INSERT INTO "+this.TABLE_NAME+" (sender, receiver, message) VALUES ('" + sender + "', '" + receiver + "', '" + message + "');");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /*
     * Returns ArrayList containing String arrays, which contain id, sender, receiver and message
     */
    public ArrayList getMessages(String address) {
        ArrayList<String[]> records = new ArrayList<String[]>();
        Cursor c = this.db.rawQuery("SELECT * FROM "+this.TABLE_NAME+" WHERE sender = '"+address+"' OR receiver ='"+address+"' ORDER BY id DESC", null);
        try {
            // looping through all rows and adding to list
            if (c.moveToFirst()) {
                do {
                    records.add(new String[] { c.getString(0), c.getString(1), c.getString(2), c.getString(3)});
                } while (c.moveToNext());
            }

        } finally {
            try {
                c.close();
            } catch (Exception ignore) { }
        }
        return records;
    }
}
