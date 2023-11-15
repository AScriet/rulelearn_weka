package weka.classifiers.rules;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.rulelearn.approximations.*;
import org.rulelearn.core.InvalidSizeException;
import org.rulelearn.data.InformationTable;
import org.rulelearn.data.InformationTableWithDecisionDistributions;
import org.rulelearn.data.arff.ArffReader;
import org.rulelearn.measures.dominance.EpsilonConsistencyMeasure;
import org.rulelearn.measures.dominance.RoughMembershipMeasure;
import org.rulelearn.rules.*;
import sun.awt.windows.WPrinterJob;
import tools.ArffConverter;
import tools.ArffInstance2Table;
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
        ArffInstance2Table arffC = new ArffInstance2Table();

        informationTable = arffC.convert(data);
        //informationTable = arffConverter.convert(data.toString());


        for (int i = 0; i < informationTable.getNumberOfAttributes(); i++) {
            System.out.println(informationTable.getAttribute(i).serialize());
        }
        //System.out.println(Arrays.toString(informationTable.getFields(0)));
        //System.out.println(informationTable.getDecision(0));
        InformationTableWithDecisionDistributions informationTableWithDecisionDistributions = new InformationTableWithDecisionDistributions(informationTable);

        System.out.println(informationTableWithDecisionDistributions.getDominanceConesDecisionDistributions().getNumberOfObjects());
        for (int i = 0; i < 10; i++) {
            System.out.println(informationTableWithDecisionDistributions.getDecision(i));
        }

        RuleSetWithCharacteristics resultSet = null;
        RuleSetWithComputableCharacteristics rules = null;
        //UnionsWithSingleLimitingDecision unions = new UnionsWithSingleLimitingDecision(
        //        informationTableWithDecisionDistributions,
        //        new VCDominanceBasedRoughSetCalculator(RoughMembershipMeasure.getInstance(), 0.0));

        UnionsWithSingleLimitingDecision unions = null;


        unions = new UnionsWithSingleLimitingDecision(
                informationTableWithDecisionDistributions,
                new VCDominanceBasedRoughSetCalculator(EpsilonConsistencyMeasure.getInstance(), 0.0));

        RuleInducerComponents ruleInducerComponents = null;

        ApproximatedSetProvider unionAtLeastProvider = new UnionProvider(Union.UnionType.AT_LEAST, unions);
        ApproximatedSetProvider unionAtMostProvider = new UnionProvider(Union.UnionType.AT_MOST, unions);
        ApproximatedSetRuleDecisionsProvider unionRuleDecisionsProvider = new UnionWithSingleLimitingDecisionRuleDecisionsProvider();


        ruleInducerComponents = new PossibleRuleInducerComponents.Builder().build();

        rules = (new VCDomLEM(ruleInducerComponents, unionAtLeastProvider, unionRuleDecisionsProvider)).generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(""));
        rules.calculateAllCharacteristics();
        resultSet = rules;

        rules = (new VCDomLEM(ruleInducerComponents, unionAtMostProvider, unionRuleDecisionsProvider)).generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(""));
        rules.calculateAllCharacteristics();
        resultSet = RuleSetWithCharacteristics.join(resultSet, rules);


        System.out.println(resultSet.size());
        System.out.println(resultSet.serialize());
        //Unions unions = new Unions(new VCDominanceBasedRoughSetCalculator(RoughMembershipMeasure.getInstance(), 0.1))




    }

    @Override
    public double classifyInstance(Instance instance) throws Exception {
        return 1.0;
    }

    public static void main(String[] args){
        args = new String[] {
                "-t", "C:\\Users\\AScriet\\Desktop\\data1.arff",
                "-T", "C:\\Users\\AScriet\\Desktop\\data1.arff"
        };
        runClassifier(new DRSA(), args);
    }


}
