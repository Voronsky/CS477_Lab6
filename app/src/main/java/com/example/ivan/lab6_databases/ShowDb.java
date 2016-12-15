package com.example.ivan.lab6_databases;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ShowDb extends AppCompatActivity {
    MyDbHelper mDbHelper;
    SQLiteDatabase mDb;
    Cursor dbCursor;
    SimpleCursorAdapter dbAdapter;

    String[] COLUMNS = new String[] {MyDbHelper.COL_NAME,MyDbHelper.COL_INFO};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_db);
        mDbHelper = new MyDbHelper(this);
        mDb = mDbHelper.getWritableDatabase();
        String[] allColumns = new String[] {"_id", MyDbHelper.COL_NAME, MyDbHelper.COL_INFO};
        dbCursor = mDb.query(MyDbHelper.TABLE_NAME, allColumns, null, null, null, null, null);
        if (dbCursor!= null) dbCursor.moveToFirst();
        dbAdapter = new SimpleCursorAdapter(getBaseContext(),
                R.layout.line,
                dbCursor,
                COLUMNS,
                new int[]{R.id.tv_name, R.id.tv_details}, 0);
        ListView listView = (ListView) findViewById(R.id.dblist);
        // Assign adapter to ListView
        listView.setAdapter(dbAdapter);

    }



}
