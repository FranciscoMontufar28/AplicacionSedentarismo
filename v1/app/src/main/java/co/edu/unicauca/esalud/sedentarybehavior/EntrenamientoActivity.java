package co.edu.unicauca.esalud.sedentarybehavior;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import co.edu.unicauca.esalud.sedentarybehavior.Background.Contador;

public class EntrenamientoActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private String buff;
    private String mGyrox, mGyroy, mGyroz, mAccex, mAccey, mAccez;
    private int t=0;
    private android.hardware.Sensor mAccelerometer;


    private final ArrayList<Sensor> sensors = new ArrayList<Sensor>();

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if (msg.what == Contador.SECOND)
            {
                int second = msg.arg2;
                int minutes = msg.arg1;
                if (second<10) {
                    reloj.setText("0" + minutes + ":" + "0"+second);
                }else {
                reloj.setText("0"+minutes+":"+second);}

            }else {
                reloj.setText("00:00");
                Parar();
                finishAndSaveReading();
                Toast.makeText(EntrenamientoActivity.this, "Datos Guardados", Toast.LENGTH_LONG).show();

            }
        }

    };

    TextView reloj;
    Button btnIniciar, btnParar;
    Contador thread;

    String actividad, Id;

    //region Utilizado por ESTIMOTE
    /********************** Utilizado por ESTIMOTE *******************************************/

    int current_location=0, current_location2=0;

    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    // TODO: replace "<major>:<minor>" strings to match your own beacons.
    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("51275:27582", new ArrayList<String>() {{
            add("Cama");
            // se lee: "cama" esta mas cercana
            // al beacon con major 9682 y minor 5279
        }});

        placesByBeacons.put("11637:25398", new ArrayList<String>() {{
            add("Escritorio");
        }});

        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }

    private BeaconManager beaconManager;
    private Region region;
    /*****************************************************************/
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento);

        actividad = getIntent().getStringExtra("Actividad");
        Id = getIntent().getStringExtra("id");

        thread = null;
        reloj = (TextView) findViewById(R.id.RelojConteo);

        btnIniciar = (Button) findViewById(R.id.BtnIniciarEntrenamieto);
        btnParar = (Button) findViewById(R.id.BtnPararEntrenamiento);

        btnIniciar.setOnClickListener(this);
        btnParar.setOnClickListener(this);
        reloj.setText("03"+":"+"00");



        //region Estimote
        /****************************** ESTIMOTE ***********************************/

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {

                    /**Se detecta cual beacon esta mas cerca y se relaciona con la ubicacion que representa**/
                    Beacon nearestBeacon = list.get(0);
                    List<String> place = placesNearBeacon(nearestBeacon);
                    // 'TODO: update the UI here
                    /**Log.d("Beacon", "lugar inmediato: " + place);
                     Log.d("Beacon", String.valueOf(place));**/

                    /**Se calcula la proximidad al beacon, si esta cerca, se pone el indicador adecuado en el dataset**/
                    Utils.Proximity proximity = Utils.computeProximity(nearestBeacon);
                    if (proximity == Utils.Proximity.FAR || proximity == Utils.Proximity.NEAR || proximity == Utils.Proximity.IMMEDIATE) {

                        if (String.valueOf(place).equals("[Cama]")) {
                            Log.d("LUGAR", "tv");
                            current_location = 1;
                        }
                        if (String.valueOf(place).equals("[Escritorio]")) {
                            Log.d("LUGAR", "Escritorio");
                            current_location = 2;
                        }
                    } else {
                        current_location = 0;
                    }

                    if (list.size() > 1) {
                        Beacon nearestBeacon2 = list.get(1);
                        List<String> place2 = placesNearBeacon(nearestBeacon2);
                        /**Se calcula la proximidad al  2 beacon, si esta cerca, se pone el indicador adecuado en el dataset**/
                        Utils.Proximity proximity2 = Utils.computeProximity(nearestBeacon2);
                        if (proximity2 == Utils.Proximity.FAR || proximity2 == Utils.Proximity.NEAR || proximity2 == Utils.Proximity.IMMEDIATE) {

                            if (String.valueOf(place2).equals("[Cama]")) {
                                Log.d("LUGAR", "tv");
                                current_location2 = 1;
                            }
                            if (String.valueOf(place2).equals("[Escritorio]")) {
                                Log.d("LUGAR", "Escritorio");
                                current_location2 = 2;
                            }
                        } else {
                            current_location2 = 0;
                        }
                    }


                    /** else if (proximity == Utils.Proximity.FAR) {
                     Log.d("DISTANCIA A BEACON", "lejos");
                     }else if (proximity == Utils.Proximity.IMMEDIATE) {
                     Log.d("DISTANCIA A BEACON", "muy cerca");**/
                } else {

                    /**Como no se detectan Beacons, el indicador retorna a "0" **/
                    current_location = 0;
                    current_location2 = 0;

                }
            }


        });
        region = new Region("ranged region", UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), null, null);
        /*****************************************************************/
        //endregion


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.BtnIniciarEntrenamieto:
                Iniciar();
                break;
            case R.id.BtnPararEntrenamiento:
                break;
        }

    }


    private void finishAndSaveReading() {

        Calendar c = Calendar.getInstance();
        String date = Integer.toString(c.get(Calendar.DATE));
        String hour = Integer.toString(c.get(Calendar.HOUR));
        String minutes = Integer.toString(c.get(Calendar.MINUTE));
        String seconds = Integer.toString(c.get(Calendar.SECOND));
        String am = Integer.toString(c.get(Calendar.AM_PM));


        // Get/create our application's save folder
        try {

            String filename = Id + " telefono " + actividad + " " + date+ " "+hour+":"+minutes+":"+seconds+":"+am+ ".csv";
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/");
            dir.mkdir();
            // Create the file in the <activity name>-<sensor name>-<system time>.csv format
            File file = new File(dir, filename);
            FileOutputStream outputStream = new FileOutputStream(file);
            // Write the colunm headers
            outputStream.write("id,Time(ms),X(mG),Y(mG),Z(mG),Location,Location2,Classification\n".getBytes());
            outputStream.write(buff.getBytes());
            outputStream.close();
            // Workaround for Android bug #38282
            MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.w("MainActivity", sensors.toString());
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
/*
        long time = System.currentTimeMillis() + TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccex =" "+ Float.toString(event.values[0]);
            mAccey =" "+ Float.toString(event.values[1]);
            mAccez =" "+ Float.toString(event.values[2]);
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mGyrox =" "+ Float.toString(event.values[0]);
            mGyroy =" "+ Float.toString(event.values[1]);
            mGyroz =" "+ Float.toString(event.values[2]);
        }


        /**creo un buffer para descargar el archivo con los datos*//*

        String sensorRead = "10"+","+time+","+mAccex+","+mAccey+","+mAccez+","+mGyrox+","+mGyroy+","+mGyroz+","+current_location+","+current_location2+","+actividad+";";
        //String sensorRead = "10"+","+time+","+event.values[SensorManager.DATA_X]+","+event.values[SensorManager.DATA_Y]+","+event.values[SensorManager.DATA_Z]+","+mGyrox+","+mGyroy+","+mGyroz+","+current_location+","+current_location2+","+actividad+";";
        buff = buff +"\n"+ sensorRead;



*/
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

    }

    private void Parar() {
        beaconManager.disconnect();
        // stop accelerometer recording
        SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        List<android.hardware.Sensor> sensors = mSensorManager.getSensorList(android.hardware.Sensor.TYPE_ACCELEROMETER);
        mSensorManager.unregisterListener(this);
        mAccelerometer = null;

    }

    private void Iniciar() {

        //String actividad = getIntent().getStringExtra("Actividad");
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        /**asegurarse de que el telefono tenga acelerometro**/
        if (accelerometer !=null) {
            /** Elijo una frecuencia de muestreo de 25hz 40 milisegundos (40000 microsegundos)**/
           // sm.registerListener(this, sensors.get(0), 40000);
            //sm.registerListener(this, sensorss.get(0), 40000);

            sm.registerListener(new SensorListener(10,"accelerometer"),accelerometer,40000);
            sm.registerListener(new SensorListenerGyroscope(10,"gyroscope"),gyroscope,40000);

            //buff = buff + "prueba "+"\n";

            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    beaconManager.startRanging(region);
                }
            });

        }

        if (thread == null){
            thread = new Contador(handler);
            thread.start();
        }

    }


    private class SensorListener implements SensorEventListener{


        long userId;
        String sensor;

        public SensorListener(long userId, String sensor) {
            this.userId = userId;
            this.sensor = sensor;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mAccex =" "+ Float.toString(event.values[0]);
                mAccey =" "+ Float.toString(event.values[1]);
                mAccez =" "+ Float.toString(event.values[2]);
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private class SensorListenerGyroscope implements SensorEventListener{


        long userId;
        String sensor;

        public SensorListenerGyroscope(long userId, String sensor) {
            this.userId = userId;
            this.sensor = sensor;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            long time = System.currentTimeMillis() + TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();


            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                mGyrox =" "+ Float.toString(event.values[0]);
                mGyroy =" "+ Float.toString(event.values[1]);
                mGyroz =" "+ Float.toString(event.values[2]);
            }


            /**creo un buffer para descargar el archivo con los datos**/


            String sensorRead = Id+","+time+","+mAccex+","+mAccey+","+mAccez+","+mGyrox+","+mGyroy+","+mGyroz+","+current_location+","+current_location2+","+actividad+";";
            buff = buff +"\n"+ sensorRead;

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

}
