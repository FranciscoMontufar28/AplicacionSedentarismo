package me.jbakita.pebbledatalogging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataLogReceiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.ArrayList;

import me.jbakita.pebbledatalogging.Background.Contador;
import weka.gui.Main;

/**
 * MainActivity class
 * Implements a PebbleDataLogReceiver to process received log data,
 * as well as a finished session.
 */
public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener{

    private String id, activity_name2, buff;
    private android.hardware.Sensor mAccelerometer;

    private final ArrayList<Sensor> sensors = new ArrayList<Sensor>();
    private ArrayAdapter<Sensor> adapter;
    private Button startStopButton, saveButton, modelGenerateButton, classificationButton;
    EditText idEditText;
    RadioGroup radioGroup_Activities;
    RadioButton radioButton_tv_sitting;
    RadioButton radioButton_tv_lying;
    RadioButton radioButton_computer;
    RadioButton radioButton_eating;
    RadioButton radioButton_driving;
    RadioButton radioButton_transport;
    TextView reloj;
    Contador thread;

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
                Toast.makeText(MainActivity.this, "Actividad Entrenada", Toast.LENGTH_LONG).show();
                reloj.setText("03"+":"+"00");
                // End recording
                startStopButton.setText("EMPEZAR");
                saveButton.setEnabled(true);
                // stop beacon detection
                beaconManager.stopRanging(region);
                // stop accelerometer recording
                SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
                List<android.hardware.Sensor> sensors = mSensorManager.getSensorList(android.hardware.Sensor.TYPE_ACCELEROMETER);
                //mSensorManager.unregisterListener(this);
                mAccelerometer = null;
                thread = null;
            }
        }

    };


    //region Utilizado por ESTIMOTE
    /********************** Utilizado por ESTIMOTE *******************************************/

    int current_location=0, current_location2=0;

    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    // TODO: replace "<major>:<minor>" strings to match your own beacons.
    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("51275:57582", new ArrayList<String>() {{
            add("Cama");
            // se lee: "cama" esta mas cercana
            // al beacon con major 9682 y minor 5279
        }});

        placesByBeacons.put("11637:25398", new ArrayList<String>() {{
            add("Escritorio");
        }});

        placesByBeacons.put("52330:18150", new ArrayList<String>() {{
            add("Carro");
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
        setContentView(R.layout.activity_main);

        //region findViewById y configuracion de botones
        startStopButton = (Button)findViewById(R.id.startstopbutton);
        startStopButton.setOnClickListener(this);
        startStopButton.setText("EMPEZAR");

        //Setup model generate button
        modelGenerateButton = (Button)findViewById(R.id.modelgeneratebutton);
        modelGenerateButton.setOnClickListener(this);
        modelGenerateButton.setText("Generar modelo");

        // Setup save button
        saveButton = (Button)findViewById(R.id.savebutton);
        saveButton.setOnClickListener(this);
        saveButton.setText("GUARDAR");
        saveButton.setEnabled(false);


        // Setup ID edit text

        idEditText = (EditText)findViewById(R.id.editText_id);
        radioGroup_Activities = (RadioGroup)findViewById(R.id.radioGroup_Activities);
        radioButton_tv_sitting = (RadioButton)findViewById(R.id.radioButton_tv_sitting);
        radioButton_tv_lying = (RadioButton)findViewById(R.id.radioButton_tv_lying);
        radioButton_computer = (RadioButton)findViewById(R.id.radioButton_computer);
        //radioButton_eating = (RadioButton)findViewById(R.id.radioButton_eating);
        radioButton_driving = (RadioButton)findViewById(R.id.radioButton_driving);
        radioButton_transport = (RadioButton)findViewById(R.id.radioButton_transport);

        reloj = (TextView) findViewById(R.id.RelojConteo);
        //endregion

        reloj.setText("03"+":"+"00");

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.d("LOG", "aplicacion iniciada");

    }

    @Override
    protected void onResume() {
        super.onResume();
        //estimote requires turn on BT and to access GPS location?
        SystemRequirementsChecker.checkWithDefaultDialogs(this);


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
                    // TODO: update the UI here
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
                        if (String.valueOf(place).equals("[Carro]")) {
                            Log.d("LUGAR", "Carro");
                            current_location = 3;
                        }
                    } else {
                        current_location = 0;
                    }

                    if(list.size()>1) {
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
                            if (String.valueOf(place).equals("[Carro]")) {
                                Log.d("LUGAR", "Carro");
                                current_location = 3;
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


        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Always unregister callbacks
        //if(dataloggingReceiver != null) {
        //    unregisterReceiver(dataloggingReceiver);
        //}
        //SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        //mSensorManager.unregisterListener(this, mAccelerometer);
    }

    private void finishAndSaveReading() {

        // Get/create our application's save folder
        String filename = "rawdata.csv";
        String file_dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/";
        File file = new File(file_dir, filename);

        if (file.exists()){
            Log.d("CREACION DE ARCHIVO","el archivo EXISTE");
            try {

                FileOutputStream outputStream = new FileOutputStream(file);
                // Write the colunm headers
                outputStream.write(buff.getBytes());
                outputStream.close();
                // Workaround for Android bug #38282
                MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.w("MainActivity", sensors.toString());

        }else{
            Log.d("CREACION DE ARCHIVO","el archivo NO existe");
            try {

                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/");
                dir.mkdir();
                // Create the file in the <activity name>-<sensor name>-<system time>.csv format
                File file1 = new File(dir, filename);
                FileOutputStream outputStream = new FileOutputStream(file1);
                // Write the colunm headers
                outputStream.write(buff.getBytes());
                outputStream.close();
                // Workaround for Android bug #38282
                MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.w("MainActivity", sensors.toString());
        }
    }

    private AlertDialog displayDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title)
                .setNeutralButton("OK", null);
        AlertDialog dia = builder.create();
        dia.show();
        return dia;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        long time = System.currentTimeMillis() + TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
        /**creo un buffer para descargar el archivo con los datos**/
        String sensorRead = id+","+time+","+event.values[SensorManager.DATA_X]+","+event.values[SensorManager.DATA_Y]+","+event.values[SensorManager.DATA_Z]+","+current_location+","+current_location2+","+activity_name2+";";
        buff = buff +"\n"+ sensorRead;
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

        if(v.equals(startStopButton)) {
        if (idEditText.getText().length() == 0) {
            displayDialog("ID", "Ingresa el ID del participante");
        } else {
            if (startStopButton.getText().equals("EMPEZAR")){
                // Start recording

                id = idEditText.getText().toString();

                /**Obtengo la actividad que se va a realizar**/

                activity_name2 = "";
                if (radioButton_tv_sitting.isChecked()) {
                    activity_name2 = "watching-TV-sitting";
                } else if (radioButton_tv_lying.isChecked()) {
                    activity_name2 = "watching-TV-lying";
                } /*else if (radioButton_eating.isChecked()) {
                    activity_name2 = "Breakfast-lunch";
                } */else if (radioButton_computer.isChecked()) {
                    activity_name2 = "using-computer";
                } else if (radioButton_driving.isChecked()) {
                    activity_name2 = "driving-car";
                } else if (radioButton_transport.isChecked()) {
                    activity_name2 = "transported-by-car";
                }

                if (activity_name2.length() != 0 && id.length() != 0) {

                    /**declarar el sensor**/
                    SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
                    List<android.hardware.Sensor> sensors = sm.getSensorList(android.hardware.Sensor.TYPE_ACCELEROMETER);


                    /**asegurarse de que el telefono tenga acelerometro**/
                    if (sensors.size() > 0) {
                        /** Elijo una frecuencia de muestreo de 25hz 40 milisegundos (40000 microsegundos)**/
                        sm.registerListener(this, sensors.get(0), 40000);
                        beaconManager.startRanging(region);
                    }

                }

                if (thread == null){
                    thread = new Contador(handler);
                    thread.start();
                }

                startStopButton.setText("PARAR");
                saveButton.setEnabled(false);
            } else {
                // End recording
                startStopButton.setText("EMPEZAR");
                saveButton.setEnabled(true);
                // stop beacon detection
                beaconManager.stopRanging(region);
                // stop accelerometer recording
                SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
                List<android.hardware.Sensor> sensors = mSensorManager.getSensorList(android.hardware.Sensor.TYPE_ACCELEROMETER);
                mSensorManager.unregisterListener(this);
                mAccelerometer = null;


            }

        }
        }else{
            if(v.equals(saveButton)){

                finishAndSaveReading();
                Toast.makeText(MainActivity.this,"Datos Guardados", Toast.LENGTH_LONG).show();

            }else{
                if (v.equals(modelGenerateButton)){
                    //Toast.makeText(MainActivity.this,"Generar modelo", Toast.LENGTH_SHORT).show();

                    //obtenener dataset.arff
                    FeatureExtraction extraction = new FeatureExtraction();
                    extraction.Extraction();
                    Log.d("FEATURES", "dataset generado");


                    //obtener modelo con weka
                    String datasetpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/"+"dataset.arff";
                    String modelpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/"+"Modelo.model";

                    try{
                        clsModelo modelo = new clsModelo(datasetpath);
                        modelo.generarModelo(modelpath);
                        Toast.makeText(MainActivity.this,"Modelo Generado", Toast.LENGTH_SHORT).show();
                    }catch (Exception ex){ex.printStackTrace();}

                }
            }
        }
    }

}