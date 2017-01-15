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
    Button toggleButton, clearButton, leftButton, rightButton, redButton, greenButton, blueButton;

    Boolean stopped = false;

    final double MILE_CONVERSION = 0.000372823;
    final double MILLISECOND_TO_HOUR_CONVERSION = 2.77778 / 10000000;

    final int NONE = 0;
    final int RED = 1;
    final int GREEN = 2;
    final int BLUE = 3;

    double hoursTraveled = 0;
    double startTime = System.currentTimeMillis();
    double currentTime;

    double speed = 13411.2 * MILE_CONVERSION;
    double distanceTraveled = speed * hoursTraveled;

    int color = NONE;

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

        toggleButton = (Button) findViewById(R.id.toggleButton);
        clearButton = (Button) findViewById(R.id.clearButton);

        leftButton = (Button) findViewById(R.id.leftButton);
        rightButton = (Button) findViewById(R.id.rightButton);

        redButton = (Button) findViewById(R.id.redButton);
        greenButton = (Button) findViewById(R.id.greenButton);
        blueButton = (Button) findViewById(R.id.blueButton);

        final Handler handler=new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                // upadte textView here

                if(!stopped) {
                    hoursTraveled = (System.currentTimeMillis() - startTime) * MILLISECOND_TO_HOUR_CONVERSION;

                    System.out.println(hoursTraveled);

//                    double speed = 13411.2 * 12 * MILE_CONVERSION;
                    try {
                        speed = btSocket.getInputStream().read() * MILE_CONVERSION;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    double distanceTraveled = speed * hoursTraveled;

                    String speedAsString = String.format("%.2f", speed);
                    String distanceTraveledAsString = String.format("%.2f", distanceTraveled);

                    speedTextView.setText("Speed: " + speedAsString + " mph");
                    distanceTraveledTextView.setText("Distance Traveled: " + distanceTraveledAsString + " miles");
                }

                handler.postDelayed(this, 500); // set time here to refresh textView
            }
        });

        new ConnectBT().execute(); //Call the class to connect

    }

    public void toggleButtonOnClick(View v) {
        if(stopped == false) {
            stopped = true;
            toggleButton.setText("Start");

            currentTime = System.currentTimeMillis() - startTime;
        } else {
            stopped = false;
            toggleButton.setText("Stop");
            if(hoursTraveled == 0) {
                startTime = System.currentTimeMillis();
            } else {
                startTime = System.currentTimeMillis() - currentTime;
            }
        }
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

    public void leftButtonOnClick(View v) {
        if (btSocket!=null)
        {
            try {
                btSocket.getOutputStream().write("4".toString().getBytes());

                System.out.println("Write 4");
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    public void rightButtonOnClick(View v) {
        if (btSocket!=null)
        {
            try {
                btSocket.getOutputStream().write("5".toString().getBytes());

                System.out.println("Write 5");
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void turnOff()
    {
        if (btSocket!=null)
        {
            try {
                btSocket.getOutputStream().write("0".toString().getBytes());
                System.out.println("Write 0");
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    public void redButtonOnClick(View v) {
        if(color == RED) {
            color = NONE;
            System.out.println("NONE");
            turnOff();
        } else {
            color = RED;
            System.out.println("RED");
            turnOnRed();
        }
    }

    private void turnOnRed() {
        if (btSocket!=null)
        {
            try {
                btSocket.getOutputStream().write("1".toString().getBytes());

                System.out.println("Write 1");
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    public void greenButtonOnClick(View v) {
        if(color == GREEN) {
            color = NONE;
            System.out.println("NONE");
            turnOff();
        } else {
            color = GREEN;
            System.out.println("GREEN");
            turnOnGreen();
        }
    }

    private void turnOnGreen() {
        if (btSocket!=null)
        {
            try {
                btSocket.getOutputStream().write("2".toString().getBytes());
                System.out.println("Write 2");
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    public void blueButtonOnClick(View v) {
        if(color == BLUE) {
            color = NONE;
            System.out.println("NONE");
            turnOff();
        } else {
            color = BLUE;
            System.out.println("BLUE");
            turnOnBlue();
        }
    }

    private void turnOnBlue() {
        if (btSocket!=null)
        {
            try {
                btSocket.getOutputStream().write("3".toString().getBytes());
                System.out.println("Write 3");
            } catch (IOException e) {
                msg("Error");
            }
        }
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
