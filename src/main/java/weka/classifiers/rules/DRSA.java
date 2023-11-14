package weka.classifiers.rules;

import org.rulelearn.approximations.Union;
import org.rulelearn.data.InformationTable;
import org.rulelearn.data.arff.ArffReader;
import org.rulelearn.rules.ApproximatedSetProvider;
import org.rulelearn.rules.UnionProvider;
import sun.awt.windows.WPrinterJob;
import tools.ArffConverter;
import weka.classifiers.AbstractClassifier;
import weka.core.*;

import java.util.Arrays;
import java.util.Vector;

public class DRSA extends AbstractClassifier implements
        TechnicalInformationHandler {



    public String globalInfo(){
        return "test info about DRSA" + getTechnicalInformation().toString();
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        return null;
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        super.setOptions(options);
    }



    @Override
    public void buildClassifier(Instances data) throws Exception {

        InformationTable informationTable;
        ArffConverter arffConverter = new ArffConverter();

        informationTable = arffConverter.convert(data.toString());

        for (int i = 0; i < informationTable.getNumberOfAttributes(); i++) {
            System.out.println(informationTable.getAttribute(i).serialize());
        }
    }

    @Override
    public double classifyInstance(Instance instance) throws Exception {
        return 1.0;
    }

    public static void main(String[] args){
        args = new String[] {
                "-t", "C:\\Users\\AScriet\\Desktop\\test.arff",
                "-T", "C:\\Users\\AScriet\\Desktop\\test.arff"
        };
        runClassifier(new DRSA(), args);
    }


}
