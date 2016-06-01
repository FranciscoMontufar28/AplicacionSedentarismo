package me.jbakita.pebbledatalogging;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class clsModelo {
    private final Classifier Classifier = new RandomForest();
    private final Instances train;

    public clsModelo(String rutaCorpus) throws Exception {
        train = ConverterUtils.DataSource.read(rutaCorpus);
        train.setClassIndex(0);
    }

    /*
        Función que genera el modelo en base al corpus de entrenamiento arff.
        Recibe como parámetro la ruta donde deseamos generar el modelo.
    */
    public void generarModelo(String rutaDestino)throws Exception{
        if(train.numInstances()==0)
            throw new Exception("No classifier available");
        weka.core.SerializationHelper.write(rutaDestino, Classifier);
    }
}