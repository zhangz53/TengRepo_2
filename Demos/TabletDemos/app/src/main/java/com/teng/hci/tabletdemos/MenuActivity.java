package com.teng.hci.tabletdemos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;


public class MenuActivity extends ActionBarActivity {

    private int taskIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button videoButton = (Button)findViewById(R.id.videoBtn);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskIndex = 1;
                new AsyncCaller().execute();
            }
        });

        Button musicButton = (Button)findViewById(R.id.musicBtn);
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskIndex = 2;
                new AsyncCaller().execute();
            }
        });

        Button bookButton = (Button)findViewById(R.id.bookBtn);
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskIndex = 3;
                new AsyncCaller().execute();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class AsyncCaller extends AsyncTask<Void, Void, Void>
    {
        ProgressDialog pdLoading = new ProgressDialog(MenuActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage("\tConnecting via Bluetooth...");
            pdLoading.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            BluetoothReceiver.getInstance().activity = MenuActivity.this;
            try {
                BluetoothReceiver.OpenBT();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pdLoading.dismiss();
            Intent mainIntent;
            switch (taskIndex)
            {
                case 1:
                    mainIntent = new Intent(MenuActivity.this,VideoActivity.class);
                    break;
                case 2:
                    mainIntent = new Intent(MenuActivity.this,MusicActivity.class);
                    break;
                case 3:
                    mainIntent = new Intent(MenuActivity.this,BookActivity.class);
                    break;
                default:
                    mainIntent = new Intent(MenuActivity.this,BookActivity.class);
                    break;
            }
            MenuActivity.this.startActivity(mainIntent);
        }

    }
}
