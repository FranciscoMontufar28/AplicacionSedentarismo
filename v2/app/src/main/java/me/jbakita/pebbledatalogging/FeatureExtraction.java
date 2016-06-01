package me.jbakita.pebbledatalogging;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class FeatureExtraction {

    public static int number_of_lines = 150;
    float[] xAvr;
    float[] yAvr;
    float[] zAvr;
    float[] ubicationAvr;
    float[] ubication2Avr;
    float[] xAbsDev;
    float[] yAbsDev;
    float[] zAbsDev;
    float[] xSDiv;
    float[] ySDiv;
    float[] zSDiv;
    float[] AvrMagnitude;

    public void Extraction() {

        String dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/"+"rawdata.csv";

        String texto = leerTxt(dir);

        String[] lineas = texto.split(";");
        int numero_de_lineas_total = lineas.length;
        int numero_de_muestras = numero_de_lineas_total/number_of_lines;
        System.out.println("numero de muestras: "+ numero_de_muestras);

        xAvr = new float[numero_de_muestras];
        yAvr= new float[numero_de_muestras];
        zAvr= new float[numero_de_muestras];
        ubicationAvr= new float[numero_de_muestras];
        ubication2Avr= new float[numero_de_muestras];
        xAbsDev= new float[numero_de_muestras];
        yAbsDev= new float[numero_de_muestras];
        zAbsDev= new float[numero_de_muestras];
        xSDiv= new float[numero_de_muestras];
        ySDiv= new float[numero_de_muestras];
        zSDiv= new float[numero_de_muestras];
        AvrMagnitude= new float[numero_de_muestras];


        File f;
        FileWriter w;
        BufferedWriter bw;
        PrintWriter wr;

        try{

            f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/"+"dataset.arff");
            w = new FileWriter(f);
            bw = new BufferedWriter(w);
            wr = new PrintWriter(bw);


            wr.write("@relation sedentary");
            wr.append("\n");
            wr.append("\n@attribute sedentary {watching-TV-sitting, watching-TV-lying, using-computer, driving-car, transported-by-car}");
            wr.append("\n@attribute XAVG numeric");
            wr.append("\n@attribute YAVG numeric");
            wr.append("\n@attribute ZAVG numeric");
            wr.append("\n@attribute XSTANDDEV numeric");
            wr.append("\n@attribute YSTANDDEV numeric");
            wr.append("\n@attribute ZSTANDDEV numeric");
            wr.append("\n@attribute XABSOLDEV numeric");
            wr.append("\n@attribute YABSOLDEV numeric");
            wr.append("\n@attribute ZABSOLDEV numeric");
            wr.append("\n@attribute RESULTANT numeric");
            wr.append("\n@attribute UBICATIONAVG numeric");
            wr.append("\n@attribute UBICATION2AVG numeric");
            wr.append("\n");
            wr.append("\n@data");

            wr.close();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        for (int i=0;i<numero_de_muestras;i++){
            System.out.println("Obtener muestra: "+ i);
            obtenerLineas(lineas, i);
        }

    }

    public String leerTxt(String direccion){ //direccion del archivo

        String texto = "";

        try{
            BufferedReader bf = new BufferedReader(new FileReader(direccion));
            String temp = "";
            String bfRead;
            while((bfRead = bf.readLine()) != null){
                //hace el ciclo, mientras bfRead tiene datos
                temp = temp + bfRead; //guardado el texto del archivo
            }

            texto = temp;

        }catch(Exception e){
            System.err.println("No se encontro archivo");
        }

        return texto;

    }

    public void obtenerLineas(String[] lines, int sample_number){

        String[] line = lines[0].split(",");
        String user_id = line[0];
        String activity = "";

        double[] x = new double[number_of_lines];
        double[] y = new double[number_of_lines];
        double[] z = new double[number_of_lines];
        double[] ubication = new double[number_of_lines];
        double[] ubication2 = new double[number_of_lines];


        /*System.out.println(number_of_lines);
        System.out.println(lines[0]);*/

        for(int i=0;i<number_of_lines;i++){

            //magia para obtener todos los datos de manera iterativa
            String[] atributes = lines[i+(number_of_lines*sample_number)].split(",");
            activity = atributes[7];
            System.out.println(atributes[0]+" "+atributes[1]+" "+atributes[2]+" "+atributes[3]+" "+atributes[4]+" "+atributes[5]+" "+atributes[6]+" "+atributes[7]);

            x[i] = Double.parseDouble(atributes[2]);
            y[i] = Double.parseDouble(atributes[3]);
            z[i] = Double.parseDouble(atributes[4]);

            ubication[i] = Double.parseDouble(atributes[5]);
            ubication2[i] = Double.parseDouble(atributes[6]);

        }

        xAvr[sample_number] = getAvr(number_of_lines, x);
        yAvr[sample_number] = getAvr(number_of_lines, y);
        zAvr[sample_number] = getAvr(number_of_lines, z);

        ubicationAvr[sample_number] = getAvr(number_of_lines, ubication);
        ubication2Avr[sample_number] = getAvr(number_of_lines, ubication2);

        xAbsDev[sample_number] = getAbsDev(number_of_lines, x, xAvr[sample_number]);
        yAbsDev[sample_number] = getAbsDev(number_of_lines, y, yAvr[sample_number]);
        zAbsDev[sample_number] = getAbsDev(number_of_lines, z, zAvr[sample_number]);

        xSDiv[sample_number] = getSDiv(number_of_lines, x, xAvr[sample_number]);
        ySDiv[sample_number] = getSDiv(number_of_lines, y, yAvr[sample_number]);
        zSDiv[sample_number] = getSDiv(number_of_lines, z, zAvr[sample_number]);

        AvrMagnitude[sample_number] = getAvrMagnitude(number_of_lines, x, y, z);

        String ruta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/DatosExperimento/"+"dataset.arff";
        File archivo = new File(ruta);
        FileWriter w;
        BufferedWriter bw;
        PrintWriter wr;

        try{

            // el true es para que se siga escribiendo sobre el archivo
            w = new FileWriter(archivo, true);
            bw = new BufferedWriter(w);
            wr = new PrintWriter(bw);


            if(archivo.exists()) {
                wr.write("\n"+activity+","+xAvr[sample_number]+","+yAvr[sample_number]+","+ zAvr[sample_number]+ ","+xSDiv[sample_number]+","+ ySDiv[sample_number]+ ","+zSDiv[sample_number]+","+xAbsDev[sample_number]+","+yAbsDev[sample_number]+","+zAbsDev[sample_number]+","+AvrMagnitude[sample_number]+","+ubicationAvr[sample_number]+","+ubication2Avr[sample_number]);
                wr.close();
                bw.close();
            } else {
                System.out.println("No se ha creado un archivo arff aun");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*System.out.println("Promedio eje x: "+ xAvr[sample_number]);
        System.out.println("Promedio eje y: "+ yAvr[sample_number]);
        System.out.println("Promedio eje z: "+ zAvr[sample_number]);
        System.out.println("Promedio de la ubicacion: "+ ubicationAvr[sample_number]);
        System.out.println("Desviacion estandar eje x: "+ xSDiv[sample_number]);
        System.out.println("Desviacion estandar eje y: "+ ySDiv[sample_number]);
        System.out.println("Desviacion estandar eje z: "+ zSDiv[sample_number]);
        System.out.println("Media abdoluta eje x: "+ xAbsDev[sample_number]);
        System.out.println("Media abdoluta eje y: "+ yAbsDev[sample_number]);
        System.out.println("Media abdoluta eje z: "+ zAbsDev[sample_number]);

        System.out.println("Promedio aceleracion total: "+ AvrMagnitude[sample_number]);*/


    }

    public static float getAvr(int count, double[] n) {
        float avr = 0;

        for (int i = 0; i < count; i++) {
            avr += n[i];
            //System.out.println(n[i]);
        }
        avr = avr / count;

        return avr;
    }

    public static double[] getAbsSum(int count, double[] x, double[] y, double[] z){
        double [] values = new double[count];
        for (int i = 0; i < count; i++){
            values[i] = ((Math.abs(x[i])) + (Math.abs(y[i])) + (Math.abs(z[i])));
        }

        return values;
    }


    public static float getAvrAbsSum(int count, double[] x, double[] y, double[] z){
        double[] values = getAbsSum(count, x, y, z);

        return getAvr(count, values);
    }

    public static float getSDAbsSum(int count, double[] x, double[] y, double[] z){
        float avr = getAvrAbsSum(count, x, y, z);
        double[] values = getAbsSum(count, x, y, z);

        return getSDiv(count, values, avr);
    }

    /** Retorna vector de la suma de las raices cuadradas de los tres ejes**/
    public static double[] getMagnitude(int count, double[] x, double[] y, double[] z){
        double [] values = new double [count];
        double sum = 0;
        for (int i = 0; i < count; i++){
            sum = ((Math.pow(x[i],2)) + (Math.pow(y[i],2)) + (Math.pow(z[i],2)));
            values[i] = (Math.sqrt(sum));
        }

        return values;
    }

    /** Retorna la media de las raices cuadradas**/
    public static float getAvrMagnitude(int count, double[] x, double[] y, double[] z){
        double[] values = getMagnitude(count, x, y, z);

        return getAvr(count, values);
    }

    /** Retorna desviacion estandar por eje **/
    public static float getSDiv(int count, double[] n, float nAvr) {
        double nSDiv = 0; //
        //
        for (int k = 0; k < count; k++) {
            nSDiv += ((n[k] - nAvr) * (n[k] - nAvr));
        }
        nSDiv = (Math.sqrt(nSDiv)) / count;

        return (float) nSDiv;
    }

    /**Retorna desvicacion absoluta**/
    public static float getAbsDev(int count, double[] n, float navr) {
        double aDev = 0;
        for (int i = 0; i < count; i++) {
            aDev += Math.abs(n[i] - navr);
        }
        aDev = aDev / count;

        return (float) aDev;
    }
}