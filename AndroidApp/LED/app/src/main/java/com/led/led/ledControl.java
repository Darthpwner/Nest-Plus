package com.led.led;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.IOException;
import java.util.EventObject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class ledControl extends ActionBarActivity {

    Button btnOn, btnOff, btnDis, lightOn, lightOff, windowO, windowC, refresh;
    SeekBar brightness;
    TextView lumn;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private String window, door, light;
    private String objectID = "BaxG6tHg2n";
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Home");

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_led_control);
        Parse.enableLocalDatastore(getApplicationContext());

        Parse.initialize(this, "fes2eEScbm2TcZvlikMoS5nLnkUvczoIOgSNwrXw", "b2NfZuX5FARxqjaHPQEu6Hrz4bY6anaOivA0CmjI");


        //call the widgtes
        btnOn = (Button) findViewById(R.id.button2);
        btnOff = (Button) findViewById(R.id.button3);
        lightOn = (Button) findViewById(R.id.button5);
        lightOff = (Button) findViewById(R.id.button6);
        windowO = (Button) findViewById(R.id.button7);
        windowC = (Button) findViewById(R.id.button8);
        refresh = (Button) findViewById(R.id.button9);
        btnDis = (Button) findViewById(R.id.button4);
        lumn = (TextView) findViewById(R.id.lumn);

        new ConnectBT().execute(); //Call the class to connect

        //while (true) {
        //commands to be sent to bluetooth
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLed();      //method to turn on
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLed();   //method to turn off
            }
        });

        lightOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLight();   //method to turn off
            }
        });

        lightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLight();   //method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); //close connection
            }
        });

        windowC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWindow();   //method to turn off
            }
        });

        windowO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWindow();   //method to turn off
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();   //method to turn off
            }
        });

    }

    //commands to be sent to bluetooth

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }

    private void refresh() {
        // [Optional] Power your app with Local Datastore. For more info, go to
        // https://parse.com/docs/android/guide#local-datastore
        ParseObject home = new ParseObject("Home");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    query.getInBackground(objectID, new GetCallback<ParseObject>() {
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                if (window == null || !window.equals(object.getString("window"))) {
                                    window = object.getString("window");
                                    if (window.equals("OPENED")) {
                                        openWindow();
                                    } else {
                                        closeWindow();
                                    }
                                }
                                if (light == null || !light.equals(object.getString("lights"))) {
                                    light = object.getString("lights");
                                    if (light.equals("ON")) {
                                        turnOnLight();
                                    } else {
                                        turnOffLight();
                                    }
                                }
                                if (door == null || !door.equals(object.getString("door"))) {
                                    door = object.getString("door");
                                    if (door.equals("OPENED")) {
                                        turnOnLed();
                                    } else {
                                        turnOffLed();
                                    }
                                }
                            } else {
                                // something went wrong
                            }
                        }
                    });
                    //System.out.println(scoreList.size());
                    //Log.d("score", "Retrieved " + scoreList.size() + " scores");
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        }, 2000);
    }

    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("0".toString().getBytes());
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Home");
                query.whereEqualTo("objectId", objectID);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> nameList, ParseException e) {
                        if (e == null) {
                            nameList.get(0).put("door", "CLOSED");
                            nameList.get(0).saveInBackground();
                            door = "CLOSED";
                        } else {
                            Log.d("Post retrieval", "Error: " + e.getMessage());
                        }
                    }
                });
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try {
                btSocket.getOutputStream().write("1".toString().getBytes());
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Home");
                query.whereEqualTo("objectId", objectID);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> nameList, ParseException e)
                    {
                        if (e == null)
                        {
                            nameList.get(0).put("door", "OPENED");
                            nameList.get(0).saveInBackground();
                            door = "OPENED";
                        }
                        else
                        {
                            Log.d("Post retrieval", "Error: " + e.getMessage());
                        }
                    }
                });
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void turnOffLight()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("2".toString().getBytes());
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Home");
                query.whereEqualTo("objectId", objectID);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> nameList, ParseException e) {
                        if (e == null) {
                            nameList.get(0).put("lights", "OFF");
                            nameList.get(0).saveInBackground();
                            light = "OFF";
                        } else {
                            Log.d("Post retrieval", "Error: " + e.getMessage());
                        }
                    }
                });
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLight()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("3".toString().getBytes());
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Home");
                query.whereEqualTo("objectId", objectID);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> nameList, ParseException e) {
                        if (e == null) {
                            nameList.get(0).put("lights", "ON");
                            nameList.get(0).saveInBackground();
                            light = "ON";
                        } else {
                            Log.d("Post retrieval", "Error: " + e.getMessage());
                        }
                    }
                });
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void closeWindow()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("5".toString().getBytes());
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Home");
                query.whereEqualTo("objectId", objectID);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> nameList, ParseException e) {
                        if (e == null) {
                            nameList.get(0).put("window", "CLOSED");
                            nameList.get(0).saveInBackground();
                            window = "CLOSED";
                        } else {
                            Log.d("Post retrieval", "Error: " + e.getMessage());
                        }
                    }
                });
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void openWindow()
    {
        if (btSocket!=null)
        {
            try {
                btSocket.getOutputStream().write("4".toString().getBytes());
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Home");
                query.whereEqualTo("objectId", objectID);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> nameList, ParseException e)
                    {
                        if (e == null)
                        {
                            nameList.get(0).put("window", "OPENED");
                            nameList.get(0).saveInBackground();
                            window = "OPENED";
                        }
                        else
                        {
                            Log.d("Post retrieval", "Error: " + e.getMessage());
                        }
                    }
                });
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
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

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
