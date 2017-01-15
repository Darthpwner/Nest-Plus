package com.led.led;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class ledControl extends ActionBarActivity {

    TextView speedTextView;
    TextView distanceTraveledTextView;
    Button stopButton, clearButton;

    final double MILE_CONVERSION = 0.000372823;
    final double MILLISECOND_TO_HOUR_CONVERSION = 2.77778 / 10000000;

    double hoursTraveled = 0;
    double startTime = System.currentTimeMillis();

    double speed = 13411.2 * MILE_CONVERSION;
    double distanceTraveled = speed * hoursTraveled;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
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
        speedTextView = (TextView) findViewById(R.id.speedTextView);
        distanceTraveledTextView = (TextView) findViewById(R.id.distanceTraveledTextView);
        stopButton = (Button) findViewById(R.id.stopButton);
        clearButton = (Button) findViewById(R.id.clearButton);

        final Handler handler=new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                // upadte textView here
                hoursTraveled = (System.currentTimeMillis() - startTime) * MILLISECOND_TO_HOUR_CONVERSION;

                System.out.println(hoursTraveled);

                double speed = 13411.2 * 12 * MILE_CONVERSION;
                double distanceTraveled = speed * hoursTraveled;

                String speedAsString = String.format("%.2f", speed);
                String distanceTraveledAsString = String.format("%.2f", distanceTraveled);

                speedTextView.setText("Speed: " + speedAsString + " mph");
                distanceTraveledTextView.setText("Distance Traveled: " + distanceTraveledAsString + " miles");

                handler.postDelayed(this, 500); // set time here to refresh textView
            }
        });

//        new ConnectBT().execute(); //Call the class to connect

    }

    public void stopButtonOnClick(View v) {
        hoursTraveled = (System.currentTimeMillis() - startTime) * MILLISECOND_TO_HOUR_CONVERSION;
        startTime = System.currentTimeMillis();

        double speed = 13411.2 * 12 * MILE_CONVERSION;  //Proof of concept works, just need to constantly update.
        double distanceTraveled = speed * hoursTraveled;

        System.out.println(MILLISECOND_TO_HOUR_CONVERSION);
        System.out.println(hoursTraveled);

        String speedAsString = String.format("%.2f", speed);
        String distanceTraveledAsString = String.format("%.2f", distanceTraveled);

        speedTextView.setText("Speed: " + speedAsString + " mph");
        distanceTraveledTextView.setText("Distance Traveled: " + distanceTraveledAsString + " miles");
        System.out.println("STOP");
    }

    public void clearButtonOnClick(View v) {
        hoursTraveled = 0;
        startTime = System.currentTimeMillis();

        double speed = 13411.2 * MILE_CONVERSION;
        double distanceTraveled = speed * hoursTraveled;

        String speedAsString = String.format("%.2f", speed);
        String distanceTraveledAsString = String.format("%.2f", distanceTraveled);

        speedTextView.setText("Speed: " + speedAsString + " mph");
        distanceTraveledTextView.setText("Distance Traveled: " + distanceTraveledAsString + " miles");

        System.out.println("CLEAR");
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
