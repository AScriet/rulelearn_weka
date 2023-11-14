package weka.classifiers.rules;

import org.rulelearn.approximations.Union;
import org.rulelearn.rules.ApproximatedSetProvider;
import org.rulelearn.rules.UnionProvider;
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
        System.out.println(data);
    }

    @Override
    public double classifyInstance(Instance instance) throws Exception {
        return 1.0;
    }

    public static void main(String[] args){
        args = new String[] {"-t", "C:\\main\\MachineLearning\\kNN\\winequality-red.csv"};
        runClassifier(new DRSA(), args);
    }


}
