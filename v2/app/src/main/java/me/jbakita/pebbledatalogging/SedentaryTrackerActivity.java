package me.jbakita.pebbledatalogging;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class SedentaryTrackerActivity extends Activity implements SensorEventListener{

    private String buff;
    int i = 0;
    ListView lista;
    ArrayAdapter<String> adaptador;
    List behavior = new ArrayList<String>();

    double[] x = new double[150];
    double[] y = new double[150];
    double[] z = new double[150];
    double[] ubication1 = new double[150];
    double[] ubication2 = new double[150];

    //region Utilizando Estimote
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
        setContentView(R.layout.activity_sedentary_tracker);

        lista = (ListView)findViewById(R.id.lista);
        adaptador = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, behavior);
        lista.setAdapter(adaptador);
    }

    protected void onDestroy(){
        super.onDestroy();
        // stop beacon detection
        beaconManager.stopRanging(region);
        // stop accelerometer recording
        SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        List<android.hardware.Sensor> sensors = mSensorManager.getSensorList(android.hardware.Sensor.TYPE_ACCELEROMETER);
        mSensorManager.unregisterListener(this);

    }

    protected void onPause(){
        super.onPause();
        // stop beacon detection
        beaconManager.stopRanging(region);
        // stop accelerometer recording
        SensorManager mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        List<android.hardware.Sensor> sensors = mSensorManager.getSensorList(android.hardware.Sensor.TYPE_ACCELEROMETER);
        mSensorManager.unregisterListener(this);

    }

    protected void onResume(){
        super.onResume();

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



        /**declarar el sensor**/
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<android.hardware.Sensor> sensors = sm.getSensorList(android.hardware.Sensor.TYPE_ACCELEROMETER);


        /**asegurarse de que el telefono tenga acelerometro**/
        if (sensors.size() > 0) {
            /** Elijo una frecuencia de muestreo de 25hz 40 milisegundos (40000 microsegundos)**/
            sm.registerListener(this, sensors.get(0), 40000);

            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    beaconManager.startRanging(region);
                }
            });

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //cada 10 segundos hago clasificacion
        if(i<150){

            String x_read = String.valueOf(event.values[SensorManager.DATA_X]);
            String y_read = String.valueOf(event.values[SensorManager.DATA_Y]);
            String z_read = String.valueOf(event.values[SensorManager.DATA_Z]);
            String cl1 = String.valueOf(current_location);
            String cl2 = String.valueOf(current_location2);

            //guardo las 250 muestras en arrays
            x[i] = Double.parseDouble(x_read);
            y[i] = Double.parseDouble(y_read);
            z[i] = Double.parseDouble(z_read);
            ubication1[i] = Double.parseDouble(cl1);
            ubication2[i] = Double.parseDouble(cl2);

            i++;

        }else{

            //cuando estan llenos los arrays, obtengo las caracteristicas

            i=0;

            // obtengo el example correspondiente a los ultimos 10 segundos (250 muestras)
            FeatureExtractionTracker FET = new FeatureExtractionTracker();
            float[] result = FET.getExample(x,y,z,ubication1,ubication2);

            //clasifico
            String datasetpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/"+"dataset.arff";
            String modelpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/"+"Modelo.model";

            try {
                clsClasificacion clasificacion = new clsClasificacion(datasetpath, modelpath);
                String resultado = clasificacion.clasificar(result);
                Toast.makeText(SedentaryTrackerActivity.this, resultado, Toast.LENGTH_SHORT).show();
                behavior.add(new String(resultado));
                adaptador.notifyDataSetChanged();
            } catch (Exception ex) {ex.printStackTrace();}


        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
