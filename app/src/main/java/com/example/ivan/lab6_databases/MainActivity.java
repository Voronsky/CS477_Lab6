package com.example.ivan.lab6_databases;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.provider.ContactsContract;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    MatrixCursor mMatrixCursor;
    SimpleCursorAdapter mAdapter;
    SQLiteDatabase mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // The contacts from the contacts content provider is stored in this cursor
        mMatrixCursor = new MatrixCursor(new String[] { "_id","name","details"} );
        // Adapter to set data in the listview
        mAdapter = new SimpleCursorAdapter(getBaseContext(),
                R.layout.line,
                null,
                new String[]{ "name","details"},
                new int[] { R.id.tv_name, R.id.tv_details}, 0);

        // Getting reference to listview
        ListView lstContacts = (ListView)findViewById(R.id.myList);
        // Setting the adapter to listview
        assert lstContacts!=null;
        lstContacts.setAdapter(mAdapter);

        // Creating an AsyncTask object to retrieve and load listview with contacts
        ListViewContactsLoader listViewContactsLoader = new ListViewContactsLoader();

        // Starting the AsyncTask process to retrieve and load listview with contacts
        listViewContactsLoader.execute();
        //setContentView(R.layout.activity_main);

        lstContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mMatrixCursor.moveToPosition(position)){
                    String name = mMatrixCursor.getString(1);
                    //0 = id, 1 = name, 2 = details
                    //Toast.makeText(getApplicationContext(),name,Toast.LENGTH_SHORT).show();
                    startInfo2DB(name);
                }
            }
        });
    }
    /** An AsyncTask class to retrieve and load listview with contacts */
    private class ListViewContactsLoader extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... params) {
            Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;

            // Querying the table ContactsContract.Contacts to retrieve all the contacts
            Cursor contactsCursor = getContentResolver().query(contactsUri, null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC ");

            if(contactsCursor.moveToFirst()){
                do{
                    long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex("_ID"));

                    Uri dataUri = ContactsContract.Data.CONTENT_URI;

                    // Querying the table ContactsContract.Data to retrieve individual items like
                    // home phone, mobile phone, work email etc corresponding to each contact
                    Cursor dataCursor = getContentResolver().query(dataUri, null,
                            ContactsContract.Data.CONTACT_ID + "=" + contactId,
                            null, null);

                    String displayName="";

                    String homeEmail="";
                    String otherEmail="";

                    if(dataCursor.moveToFirst()){
                        // Getting Display Name
                        displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME ));
                        do{

                            // Getting EMails
                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ) ) {
                                switch(dataCursor.getInt(dataCursor.getColumnIndex("data2"))){
                                    case ContactsContract.CommonDataKinds.Email.TYPE_HOME :
                                        homeEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Email.TYPE_OTHER :
                                        otherEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                }
                            }

                        }while(dataCursor.moveToNext());
                        String details = "";

                        // Concatenating various information to single string

                        if(homeEmail != null && !homeEmail.equals("") )
                            details += "HomeEmail : " + homeEmail + "\n";
                        if(otherEmail != null && !otherEmail.equals("") )
                            details += "Email : " + otherEmail + "\n";

                        // Adding id, display name, path to photo and other details to cursor
                        mMatrixCursor.addRow(new Object[]{ Long.toString(contactId),displayName,details});
                    }
                }while(contactsCursor.moveToNext());
            }
            return mMatrixCursor;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            // Setting the cursor containing contacts to listview
            mAdapter.swapCursor(result);
        }


    }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /* check to see which Menu option was selected*/
        switch(item.getItemId()){
            case R.id.showDB:
                Intent intent = new Intent(getApplicationContext(),ShowDb.class);
                startActivity(intent);
                break;
            case R.id.clearDB:
                MyDbHelper mDbHelper = new MyDbHelper(this); //Make use of the MDb helper
                mDb = mDbHelper.getWritableDatabase(); //Get the db file
                mDbHelper.onUpgrade(mDb,0,0); //Drop tables aka clear and make a new DB
                Toast.makeText(getApplicationContext(),"The database has been cleared",
                        Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    /*private void clearDatabase(){
        mDb.delete(MyDbHelper.TABLE_NAME,null,null);
    }*/

    private void startInfo2DB(String info){
        Intent intent = new Intent(this, Info2Db.class);
        intent.putExtra("info",info);
        startActivity(intent);
    }
}
