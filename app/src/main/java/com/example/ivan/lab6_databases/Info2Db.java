package com.example.ivan.lab6_databases;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Info2Db extends AppCompatActivity {
    MyDbHelper mDbHelper;
    SQLiteDatabase mDb;
    String contactName;
    String[] COLUMNS = new String[]
            {MyDbHelper.COL_NAME,MyDbHelper.COL_INFO};
    Boolean inDb;
    String originalData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info2_db);
        mDbHelper = new MyDbHelper(this);
        mDb = mDbHelper.getWritableDatabase();

        Intent intent = getIntent();
        contactName = intent.getStringExtra("info");
        Button saveBtn = (Button)findViewById(R.id.saveButton);
        TextView tv = (TextView)findViewById(R.id.intro);
        tv.setText("Additional Info for "+ contactName);
        final EditText et = (EditText)findViewById(R.id.newinfo);

        // Add query here to set up the edit field
        String selection = MyDbHelper.COL_NAME+" = ?";
        String[] args = new String[]{contactName};
        Cursor result = mDb.query(MyDbHelper.TABLE_NAME,
                COLUMNS,
                selection,args,null,null,null,null);

        if (result.moveToFirst()) {
            originalData = result.getString(1);
            et.setText(originalData);
            inDb = true;
        } else {
            et.setHint("Information to store");
            inDb = false;
        }

        /**
         * CHecks to see if a name in the DB already exists, if so write in the new value
         * else insert the new name and the comment
         */
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                Cursor dbResponse  = readContactNames();
                Boolean found;
                dbResponse.moveToFirst();
                try{
                    found = checkDatabase(contactName, dbResponse);
                    if(found){
                        clearNameFromDB(contactName);
                        values.put("Name",contactName);
                        values.put(MyDbHelper.COL_INFO,et.getText().toString());
                        mDb.insert(MyDbHelper.TABLE_NAME,null,values);
                        values.clear();
                    }else {
                        values.put("Name",contactName); //Insert the studentName into the object
                        values.put(MyDbHelper.COL_INFO,et.getText().toString());
                        mDb.insert(MyDbHelper.TABLE_NAME,null,values);
                        values.clear();
                    }
                }catch (Exception e){
                    Log.d("saveBtn","values: "+values);
                    Log.d("saveBtn","Exception:"+e);
                    Toast.makeText(getApplicationContext(),"Please check Debug",Toast.LENGTH_SHORT)
                            .show();
                }
                goBackToMain();


            }
        });


    }


    @Override
    public void onPause() {
        super.onPause();
        mDb.close();
    }



    /**
     * Check the database for this name
     * @param name - Name to look for
     * @param c - A cursor object that will be pass by value
     * @return
     */
    private boolean checkDatabase(String name, Cursor c){
        Log.d("chkDB","We got here");
        DatabaseUtils.dumpCursor(c);
        //Checks to see if there is any data at all for the column if not, Column is empty
        if(!c.moveToFirst()){
            return false;
        }
       c.moveToFirst();
       do{
            Log.d("chkDB","CursorOBj is:"+c.getString(c.getColumnIndex("Name")));
            if(c.getString(c.getColumnIndex("Name")).equals(name)){
                Log.d("chkDB","Found name: "+c.getString(c.getColumnIndex("Name")));
                return true;
            }
        }while(c.moveToNext());

        Log.d("chkDb","Did not find name in DB");
        return false;

    }


    /**
     * This makes a query to the database to delete any Name that equals the name passed
     * @param name - name to delete
     */
    private void clearNameFromDB(String name){
        mDb.delete(MyDbHelper.TABLE_NAME,"Name =? ",new String[]{name});
    }

    /**
     * This returns a Cursor object that contains the entire output of a name similar to that in question
     * @param name Name that we wish to find instances of in the database
     * @return
     */
    private Cursor readContactNames(String name){
        return mDb.query(MyDbHelper.TABLE_NAME,new String[]{"_id","Name"},"Name like "+"'%"+contactName+"%'",
                null, null, null, null);
    }

    /**
     * We will query the database and see if the user already exists and return the output
     * @return Return the output
     */
    private Cursor readContactNames(){

        return mDb.query("people",new String[]{"_id","Name"},null, new String[]{},null,null,null);
    }


    public void save2Db(View v) {
        finish();
    }

    /**
     * Go back to the main activity
     */
    private void goBackToMain(){
        Intent returnIntent = getIntent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }


}
