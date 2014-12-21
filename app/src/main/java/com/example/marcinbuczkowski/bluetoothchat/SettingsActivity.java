package com.example.marcinbuczkowski.bluetoothchat;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import java.util.Set;
import java.util.UUID;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class SettingsActivity extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private Button onBtn;
    private Button locBtn;
    private Button listBtn;
    private Button findBtn;
    private Button gpsBtn;
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    private Spinner receivers;
    private LocationManager myLocationManager;
    private String PROVIDER = LocationManager.GPS_PROVIDER;
    private Location location;
    public static Location device1;
    private Double lon;
    private Double lat;
    public static String loc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {
            onBtn.setEnabled(false);
            locBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Status: not supported");

            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            text = (TextView) findViewById(R.id.text);
            onBtn = (Button)findViewById(R.id.turnOn);
            onBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    on(v);
                }
            });

            locBtn = (Button)findViewById(R.id.distance);
            locBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    gps(v);
                }
            });

            listBtn = (Button)findViewById(R.id.paired);
            listBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    list(v);
                }
            });

            findBtn = (Button)findViewById(R.id.search);
            findBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    find(v);
                }
            });

            gpsBtn = (Button)findViewById(R.id.gps);
            gpsBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    loc(v);
                }
            });

            //myListView = (ListView)findViewById(R.id.listView1);
            this.receivers = (Spinner)findViewById(R.id.receiversSpinner);
            this.populateReceivers();
            myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            this.location = myLocationManager.getLastKnownLocation(PROVIDER);

            myLocationManager.requestLocationUpdates(
                    PROVIDER,     //provider
                    0,       //minTime
                    0,       //minDistance
                    myLocationListener); //LocationListener
            //showPosition(location);

            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            this.BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
            this.receivers.setAdapter(this.BTArrayAdapter);
            //BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            //myListView.setAdapter(BTArrayAdapter);
        }
    }

    private void showPosition(Location location)
    {
        SettingsActivity.device1=location;
    }

    private LocationListener myLocationListener
            = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            showPosition(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }};


    private void populateReceivers() {
        this.BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        Set<BluetoothDevice> pairedDevices = this.myBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //todo add address as ID and name as item content
                this.BTArrayAdapter.add(device.getAddress());
            }
        }

        this.BTArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.receivers.setAdapter(this.BTArrayAdapter);
    }

    public void on(View view){
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                    Toast.LENGTH_LONG).show();
        }
        else{
            myBluetoothAdapter.disable();
            text.setText("Status: Disconnected");
            /*Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                    Toast.LENGTH_LONG).show();*/
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                text.setText("Status: Enabled");
            } else {
                text.setText("Status: Disabled");
            }
        }
    }

    public void list(View view){
        populateReceivers();
        Toast.makeText(getApplicationContext(),"Show Paired Devices",
                Toast.LENGTH_SHORT).show();

    }

    public void loc(View view)
    {
        String person = receivers.getSelectedItem().toString();
        String [] lines = person.split("\n");
        if(SettingsActivity.device1 == null)
        {
            Toast.makeText(getApplicationContext(),"Location not found",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            String message = "NULL";
            //String message = String.valueOf(this.device1.getLongitude()) + "\n" + String.valueOf(this.device1.getLatitude());
            BluetoothClientThread btc = new BluetoothClientThread();
            BluetoothDevice bd = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(lines[0]);
            //todo replace UUID as described in MainActivity
            btc.connect(bd, UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"));
            btc.sendInfo(message, lines[0]);
            /*Toast.makeText(getApplicationContext(),message,
                    Toast.LENGTH_SHORT).show();*/
        }
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(View view) {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        }
        else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    private Double getDistance() {
        final int RADIUS_EARTH = 6371;
        double latitude1 = SettingsActivity.device1.getLatitude();
        double latitude2 = this.lat;
        double longitude1 = SettingsActivity.device1.getLongitude();
        double longitude2 = this.lon;

        double dLat = getRad(latitude2 - latitude1);
        double dLong = getRad(longitude2 - longitude1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(getRad(latitude1)) * Math.cos(getRad(latitude2)) * Math.sin(dLong / 2) * Math.sin(dLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double x = (RADIUS_EARTH * c) *1000;
        double dis = x;
        //dis *=100;
        dis = Math.round(dis);
        //dis /=100;
        return dis;
    }

    private Double getRad(Double x) {
        return x * Math.PI / 180;
    }

    //Do pokazywania odległości
    public void gps(View view){
        if(SettingsActivity.loc != null) {
            String[] temp = SettingsActivity.loc.split("\n");
            this.lon = Double.parseDouble(temp[0]);
            this.lat = Double.parseDouble(temp[1]);
            double dis = this.getDistance();
            Toast.makeText(getApplicationContext(), "Distance: " + dis,
                    Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "First find your gps position",
                    Toast.LENGTH_SHORT).show();
        }
    }


    public void visible(View view){
        Intent getVisible = new Intent(BluetoothAdapter.
                ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
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
}