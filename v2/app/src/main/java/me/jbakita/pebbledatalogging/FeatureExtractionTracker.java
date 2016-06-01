package me.jbakita.pebbledatalogging;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class FeatureExtractionTracker {

    public static int number_of_lines = 150;
    float xAvr;
    float yAvr;
    float zAvr;
    float ubicationAvr;
    float ubication2Avr;
    float xAbsDev;
    float yAbsDev;
    float zAbsDev;
    float xSDiv;
    float ySDiv;
    float zSDiv;
    float AvrMagnitude;

    public float[] getExample(double[] x, double[] y, double[] z, double[] ubication, double[] ubication2){

        //ibtengo caracteristicas
        xAvr = getAvr(number_of_lines, x);
        yAvr = getAvr(number_of_lines, y);
        zAvr = getAvr(number_of_lines, z);

        xAbsDev = getAbsDev(number_of_lines, x, xAvr);
        yAbsDev = getAbsDev(number_of_lines, y, yAvr);
        zAbsDev = getAbsDev(number_of_lines, z, zAvr);

        xSDiv = getSDiv(number_of_lines, x, xAvr);
        ySDiv = getSDiv(number_of_lines, y, yAvr);
        zSDiv = getSDiv(number_of_lines, z, zAvr);

        AvrMagnitude = getAvrMagnitude(number_of_lines, x, y, z);

        ubicationAvr = getAvr(number_of_lines, ubication);
        ubication2Avr = getAvr(number_of_lines, ubication2);

        float[] result = {xAvr, yAvr, zAvr, xAbsDev, yAbsDev, zAbsDev, xSDiv, ySDiv, zSDiv, AvrMagnitude, ubicationAvr,ubication2Avr};

        return result;

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