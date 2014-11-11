package com.example.marcinbuczkowski.bluetoothchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private BluetoothServerThread bluetoothServer;
    private UUID serverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        if (bta == null) {
            this.closeOnError("Could not find a working bluetooth adapter. Make sure your device has bluetooth support.");
        } else if (!bta.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, 1);
        }

        this.createAfterIntent();
    }

    private void createAfterIntent() {
        //instantiate the messages database before it's used; later it won't be necessary to supply a path
        try {
            ChatMessageDatabase.getInstance(this.getBaseContext().getFilesDir().getAbsolutePath());
        } catch (Exception e) { }

        //todo UUID generation based on device address and maybe other variables
        this.serverId = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");

        this.bluetoothServer = new BluetoothServerThread("BluetoothChat", this.serverId);
        new Thread(this.bluetoothServer).start();

        Button bt_settings = (Button) findViewById(R.id.bt_settings);
        Button bt_chat = (Button) findViewById(R.id.bt_chat);

        bt_settings.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        bt_chat.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //checking if user enabled bluetooth
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //continue creating activity
                this.createAfterIntent();
            } else {
                //show error and close
                this.closeOnError("You need to enable bluetooth for this application to work.");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeOnError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
