package com.example.imustreamer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.Sensor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.text.DecimalFormat;
import android.widget.Button;
import android.view.View;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.widget.EditText;
import android.os.AsyncTask;
public class MainActivity extends Activity {
    SensorManager sm = null;
    TextView textView1 = null;
    TextView textView2 = null;
    TextView textView3 = null;
    EditText edit = null;
    List list1;
    List list2;
    List list3;

    DatagramSocket socket = null;
    InetAddress inet = null;
    boolean isSocket = false;
    byte buf[] = null;

    float[][] mat = new float[][] {
            { 0, 0, 0},
            { 0, 0, 0},
            { 0, 0, 0}
    };

    DecimalFormat df = new DecimalFormat("#.##");

    SensorEventListener sel = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                float[] mag = event.values;
                textView3.setText("Magnetometer: " + df.format(mag[0]) + " " + df.format(mag[1]) + " " + df.format(mag[2]));
                mat[2][0] = mag[0];
                mat[2][1] = mag[1];
                mat[2][2] = mag[2];
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] acc = event.values;
                textView1.setText("Accelerometer: " + df.format(acc[0]) + " " + df.format(acc[1]) + " " + df.format(acc[2]));
                mat[0][0] = acc[0];
                mat[0][1] = acc[1];
                mat[0][2] = acc[2];
            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float[] gyr = event.values;
                textView2.setText("Gyroscope: " + df.format(gyr[0]) + " " + df.format(gyr[1]) + " " + df.format(gyr[2]));
                mat[1][0] = gyr[0];
                mat[1][1] = gyr[1];
                mat[1][2] = gyr[2];
            }

        }
//            float[] values = event.values;
//
//        }
    };

    protected void sendData(){
        while(isSocket)
        {
            String data = "";
            data += "acc:" + mat[0][0]+","+mat[0][1]+","+mat[0][2]+",";
            data += "gyr:" + mat[1][0]+","+mat[1][1]+","+mat[1][2]+",";
            data += "mag:"+ mat[2][0]+","+mat[2][1]+","+mat[2][2]+",";
            buf = data.getBytes(StandardCharsets.UTF_8);
            DatagramPacket dpsend = new DatagramPacket(buf,buf.length,inet,7777);
            try {
                socket.send(dpsend);
            }
            catch(Exception e)
            {
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Get a SensorManager instance */
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);

        textView1 = (TextView)findViewById(R.id.textView8);
        textView2 = (TextView)findViewById(R.id.textView9);
        textView3 = (TextView)findViewById(R.id.textView10);

        edit = (EditText)findViewById(R.id.editTextText5);

        list1 = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        list2 = sm.getSensorList(Sensor.TYPE_GYROSCOPE);
        list3 = sm.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

        if(list1.size()>0){
            sm.registerListener(sel, (Sensor) list1.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
        }

        if(list2.size()>0){
            sm.registerListener(sel, (Sensor) list2.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            Toast.makeText(getBaseContext(), "Error: No Gyroscope.", Toast.LENGTH_LONG).show();
        }

        if(list3.size()>0){
            sm.registerListener(sel, (Sensor) list3.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            Toast.makeText(getBaseContext(), "Error: No Magnetometer.", Toast.LENGTH_LONG).show();
        }

        Button button = (Button) findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // code to be executed when button is clicked
                if(!isSocket){
                    String ip = edit.getText().toString();
                    if(!ip.isEmpty()){
                        try {
                            socket = new DatagramSocket();
                            isSocket = true;
                            inet = InetAddress.getByName(ip);
                        }
                        catch(Exception e){
                            socket = null;
                            isSocket = false;
                        }
                        if(isSocket)
                        {
                            Thread thread = new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        sendData();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            thread.start();
                        }
                    }
                }
                else{
                    socket = null;
                    isSocket = false;
                }
            }
        });
    }

    @Override
    protected void onStop() {
        if(list1.size()>0){
            sm.unregisterListener(sel);
        }
        if(list2.size()>0){
            sm.unregisterListener(sel);
        }
        if(list3.size()>0){
            sm.unregisterListener(sel);
        }
        super.onStop();
    }
}