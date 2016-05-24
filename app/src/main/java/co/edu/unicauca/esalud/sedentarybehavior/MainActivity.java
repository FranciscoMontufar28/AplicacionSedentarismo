package co.edu.unicauca.esalud.sedentarybehavior;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import co.edu.unicauca.esalud.sedentarybehavior.Background.Contador;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener{

    /**declaro widgets y variables**/
    TextView x,y,z;
    private Sensor mAccelerometer;
    String buff, sensorRead;
    String activity = "";
    String user_id = "";
    public boolean sdDisponible = false;
    public boolean sdAccesoEscritura = false;
    RadioGroup radioGroupActivity;
    RadioButton radioButtonSentado_en_el_escritorio;
    RadioButton radioButtonParado_cerca_al_escritorio;
    RadioButton radioButtonAcostado_en_la_cama;
    RadioButton radioButtonSentado_en_la_cama;
    EditText editTextId;
    Button buttonGrabar;

    String currentPlace="0";


    //Contador thread;



    /********************** Utilizado por ESTIMOTE *******************************************/

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**instancio los widgets**/
        x = (TextView)findViewById(R.id.xID);
        y = (TextView)findViewById(R.id.yID);
        z = (TextView)findViewById(R.id.zID);
        radioGroupActivity = (RadioGroup)findViewById(R.id.RadioGroupActivity);
        radioButtonSentado_en_el_escritorio = (RadioButton)findViewById(R.id.radioButtonSentadoenelsescritorio);
        radioButtonParado_cerca_al_escritorio = (RadioButton)findViewById(R.id.radioButtonParadocercaalescritorio);
        radioButtonSentado_en_la_cama = (RadioButton)findViewById(R.id.radioButtonSentadoenlacama);
        radioButtonAcostado_en_la_cama = (RadioButton)findViewById(R.id.radioButtonAcostadoenlacama);
        editTextId = (EditText)findViewById(R.id.editTextId);

        buttonGrabar = (Button)findViewById(R.id.buttonGrabar);
        buttonGrabar.setOnClickListener(this);

        /**Compruebo el estado de la memoria externa (tarjeta SD)**/
        String estado = Environment.getExternalStorageState();

        if (estado.equals(Environment.MEDIA_MOUNTED))
        {
            sdDisponible = true;
            sdAccesoEscritura = true;
        }
        else if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
        {
            sdDisponible = true;
            sdAccesoEscritura = false;
        }
        else
        {
            sdDisponible = false;
            sdAccesoEscritura = false;
        }

        /**la app siempre se mantiene en portrait**/
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.d("LOG", "aplicacion iniciada");

        //thread=null;


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
                    if (proximity == Utils.Proximity.NEAR || proximity == Utils.Proximity.IMMEDIATE) {
                        Log.d("DISTANCIA A BEACON CAMA", "CERCA");
                        if (String.valueOf(place).equals("[Cama]")) {
                            Log.d("LUGAR", "cama");
                            currentPlace = "1";
                        }
                        if (String.valueOf(place).equals("[Escritorio]")) {
                            Log.d("LUGAR", "Escritorio");
                            currentPlace = "2";
                        }
                    }else{currentPlace = "0";}/** else if (proximity == Utils.Proximity.FAR) {
                        Log.d("DISTANCIA A BEACON", "lejos");
                    }else if (proximity == Utils.Proximity.IMMEDIATE) {
                        Log.d("DISTANCIA A BEACON", "muy cerca");**/
                }else{

                    /**Como no se detectan Beacons, el indicador retorna a "0" **/
                    currentPlace="0";

                }
            }


        });
        region = new Region("ranged region", UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), null, null);
        /*****************************************************************/

    }


    protected void onResume()
    {

        super.onResume();

        /**Estimote solicita encender el bt4**/
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        Log.d("LOG", "aplicacion resumida");
    }


    protected void onPause()
    {
        SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.unregisterListener(this, mAccelerometer);
        Log.d("LOG", "aplicacion en pausa");
        super.onPause();
    }


    protected void onStop()
    {
        SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.unregisterListener(this, mAccelerometer);
        Log.d("LOG", "aplicacion finalizada");
        //beaconManager.stopRanging(region);
        super.onStop();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

            if(sdDisponible ==true && sdAccesoEscritura ==true) {

                Date horaActual = new Date();
                String fecha = (horaActual.getYear() + 1900) + "" + (horaActual.getMonth() + 1) + "" + horaActual.getDate() + "" + horaActual.getHours() + "" + horaActual.getMinutes() + "" + horaActual.getSeconds();
                writeFile("dataset" + "_" +user_id + "_" + fecha + ".txt", buff);
            }
        }

        return super.onOptionsItemSelected(item);
    }



    public void writeFile(String filename, String textfile){
        try {

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/", filename );
            OutputStreamWriter outw = new OutputStreamWriter(new FileOutputStream(file));
            outw.write(textfile);
            outw.close();

        } catch (Exception e) {}
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        this.x.setText("X =" + event.values[SensorManager.DATA_X]);
        this.y.setText("Y =" + event.values[SensorManager.DATA_Y]);
        this.z.setText("Z =" + event.values[SensorManager.DATA_Z]);

        //Long tsLong = System.currentTimeMillis();
        //String ts = tsLong.toString();

        Date horaActual2 = new Date();
        String fecha2 = (horaActual2.getYear() + 1900) + "" + (horaActual2.getMonth() + 1) + "" + horaActual2.getDate() + "" + horaActual2.getHours() + "" + horaActual2.getMinutes() + "" + horaActual2.getSeconds();


        /**creo un buffer para descargar el archivo con los datos**/
        sensorRead = user_id+","+activity+","+fecha2+","+event.values[SensorManager.DATA_X]+","+event.values[SensorManager.DATA_Y]+","+event.values[SensorManager.DATA_Z]+","+currentPlace+";";
        buff = buff +"\n"+ sensorRead;

        Toast.makeText(this, currentPlace, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

        if(v.equals(buttonGrabar)) {

            /**Obtengo la actividad que se va a realizar y el id del usuario**/
            if (radioButtonSentado_en_el_escritorio.isChecked() == true) {
                activity = "Sitting_in_desk";
            } else if (radioButtonParado_cerca_al_escritorio.isChecked() == true) {
                activity = "Standing_near_the_desk";
            } else if (radioButtonAcostado_en_la_cama.isChecked() == true) {
                activity = "Lying_in_bed";
            } else if (radioButtonSentado_en_la_cama.isChecked() == true) {
                activity = "Sitting_in_bed";
            }

            user_id = editTextId.getText().toString();


            /**Valido el ingreso de los datos**/

            if(activity.length()==0){

                Toast.makeText(this, "Selecciona la actividad a realizar", Toast.LENGTH_LONG).show();

            }else if(user_id.length()==0){

                Toast.makeText(this, "Ingresa un ID", Toast.LENGTH_LONG).show();

            }

            if (activity.length()!=0 && user_id.length()!=0){

                /**declarar el sensor**/
                SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
                List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);


                /**asegurarse de que el telefono tenga acelerometro**/
                if(sensors.size()>0)
                {
                    /** Elijo una frecuencia de muestreo de 50 milisegundos (50000 microsegundos)**/
                    sm.registerListener(this, sensors.get(0), 50000);

                    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                        @Override
                        public void onServiceReady() {
                            beaconManager.startRanging(region);
                        }
                    });

                }

            }

        }
    }
}