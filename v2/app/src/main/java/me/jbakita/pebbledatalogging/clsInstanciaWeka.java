package me.jbakita.pebbledatalogging;

import java.io.IOException;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class clsInstanciaWeka {
    public Instance crearInstancia(Float XAVG,Float YAVG,Float ZAVG,Float XSTANDDEV,Float YSTANDDEV,Float ZSTANDDEV,Float XABSOLDEV,Float YABSOLDEV,Float ZABSOLDEV,Float RESULTANT,Float UBICATIONAVG,Float UBICATION2AVG, Instances train) throws IOException{
        /* El número 2, es el número de atributos que aparecen en el corpus (clima y temperatura). */
        Instance instance = new Instance(13);


        /* Se escribe sólo los atributos sin considerar las clases, en este caso, solo queda temperatura */
        Attribute atributo1 = train.attribute("XAVG");
        Attribute atributo2 = train.attribute("YAVG");
        Attribute atributo3 = train.attribute("ZAVG");
        Attribute atributo4 = train.attribute("XSTANDDEV");
        Attribute atributo5 = train.attribute("YSTANDDEV");
        Attribute atributo6 = train.attribute("ZSTANDDEV");
        Attribute atributo7 = train.attribute("XABSOLDEV");
        Attribute atributo8 = train.attribute("YABSOLDEV");
        Attribute atributo9 = train.attribute("ZABSOLDEV");
        Attribute atributo10 = train.attribute("RESULTANT");
        Attribute atributo11 = train.attribute("UBICATIONAVG");
        Attribute atributo12 = train.attribute("UBICATION2AVG");

        instance.setValue(atributo1, XAVG);
        instance.setValue(atributo2, YAVG);
        instance.setValue(atributo3, ZAVG);
        instance.setValue(atributo4, XSTANDDEV);
        instance.setValue(atributo5, YSTANDDEV);
        instance.setValue(atributo6, ZSTANDDEV);
        instance.setValue(atributo7, XABSOLDEV);
        instance.setValue(atributo8, YABSOLDEV);
        instance.setValue(atributo9, ZABSOLDEV);
        instance.setValue(atributo10, RESULTANT);
        instance.setValue(atributo11, UBICATIONAVG);
        instance.setValue(atributo12, UBICATION2AVG);

        instance.setDataset(train);

        return instance;
    }
}