package me.jbakita.pebbledatalogging;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class clsClasificacion {
    private static Instances train;
    Classifier Classifier;
    Instances data;

    public clsClasificacion(String corpus, String modelo) throws Exception{
        Classifier = (RandomForest) weka.core.SerializationHelper.read(modelo);
        train = ConverterUtils.DataSource.read(corpus);
        train.setClassIndex(0);
        data = new Instances(train);
    }

    public String clasificar(float[] atributos) throws Exception{
        double predicted;
        Instance instance;
        clsInstanciaWeka instancia = new clsInstanciaWeka();

        if(train.numInstances()==0){
            throw new Exception("No classifier available");
        }

        float XAVG = atributos[0];
        float YAVG = atributos[1];
        float ZAVG = atributos[2];
        float XSTANDDEV = atributos[3];
        float YSTANDDEV = atributos[4];
        float ZSTANDDEV = atributos[5];
        float XABSOLDEV = atributos[6];
        float YABSOLDEV = atributos[7];
        float ZABSOLDEV = atributos[8];
        float RESULTANT = atributos[9];
        float UBICATIONAVG = atributos[10];
        float UBICATION2AVG = atributos[11];

        instance = instancia.crearInstancia(XAVG,YAVG,ZAVG,XSTANDDEV,YSTANDDEV,ZSTANDDEV,XABSOLDEV,YABSOLDEV,ZABSOLDEV,RESULTANT,UBICATIONAVG,UBICATION2AVG, data);
        Classifier.buildClassifier(data);
        predicted = Classifier.classifyInstance(instance);

        return train.classAttribute().value((int)predicted);
    }
}
