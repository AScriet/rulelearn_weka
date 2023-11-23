package weka.classifiers.rules;

import org.rulelearn.approximations.*;
import org.rulelearn.data.InformationTable;
import org.rulelearn.data.InformationTableWithDecisionDistributions;
import org.rulelearn.measures.dominance.EpsilonConsistencyMeasure;
import org.rulelearn.rules.*;
import org.data.ArffConverter;
import org.data.ArffInstance2Table;
import weka.classifiers.AbstractClassifier;
import weka.core.*;
import org.rulelearn.approximations.VCDominanceBasedRoughSetCalculator;


import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

public class DRSA extends AbstractClassifier implements
        TechnicalInformationHandler {

    String unionsS = "";
    String Rules = "";
    int rulesNum;
    private double Th = 0.0;
    RuleSetWithCharacteristics resultSetModel;
    public String globalInfo(){
        return "test info about DRSA" + getTechnicalInformation().toString();
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        return null;
    }

    @Override
    public Enumeration<Option> listOptions() {
        Vector<Option> newVector = new Vector<Option>(1);
        newVector.addElement(new Option(
                "\tSet consistency threshold\n \t(Default: 0.0)\n",
                "ST",
                0,
                "-ST <consistency threshold>"));

        newVector.addAll(Collections.list(super.listOptions()));
        return newVector.elements();
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        String conThresh = Utils.getOption("ST", options);
        if (conThresh.length() != 0) {
            Th = Double.parseDouble(conThresh);
        } else {
            Th = 0.0;
        }
        //System.out.println("XYU");
        super.setOptions(options);
    }

    @Override
    public String[] getOptions() {
        Vector<String> options = new Vector<String>();
        options.add("-ST");
        options.add("" + Th);

        Collections.addAll(options, super.getOptions());

        return options.toArray(new String[0]);
    }

    @Override
    public void buildClassifier(Instances data) throws Exception {


        InformationTable informationTable;
        ArffConverter arffConverter = new ArffConverter();
        ArffInstance2Table arffC = new ArffInstance2Table();

        informationTable = arffC.convert(data);
        //informationTable = arffConverter.convert(data.toString());


        for (int i = 0; i < informationTable.getNumberOfAttributes(); i++) {
            //System.out.println(informationTable.getAttribute(i).serialize());
        }
        //System.out.println(Arrays.toString(informationTable.getFields(0)));
        //System.out.println(informationTable.getDecision(0));
        InformationTableWithDecisionDistributions informationTableWithDecisionDistributions = new InformationTableWithDecisionDistributions(informationTable);

        //System.out.println(informationTableWithDecisionDistributions.getDominanceConesDecisionDistributions().getNumberOfObjects());
        for (int i = 0; i < 10; i++) {
            //System.out.println(informationTableWithDecisionDistributions.getDecision(i));
        }


        RuleSetWithComputableCharacteristics rules = null;
        //UnionsWithSingleLimitingDecision unions = new UnionsWithSingleLimitingDecision(
        //        informationTableWithDecisionDistributions,
        //        new VCDominanceBasedRoughSetCalculator(RoughMembershipMeasure.getInstance(), 0.0));

        UnionsWithSingleLimitingDecision unions = null;



        unions = new UnionsWithSingleLimitingDecision(
                informationTableWithDecisionDistributions,
                new VCDominanceBasedRoughSetCalculator(EpsilonConsistencyMeasure.getInstance(), Th));

        RuleInducerComponents ruleInducerComponents = null;

        ApproximatedSetProvider unionAtLeastProvider = new UnionProvider(Union.UnionType.AT_LEAST, unions);
        ApproximatedSetProvider unionAtMostProvider = new UnionProvider(Union.UnionType.AT_MOST, unions);
        ApproximatedSetRuleDecisionsProvider unionRuleDecisionsProvider = new UnionWithSingleLimitingDecisionRuleDecisionsProvider();

        //ruleInducerComponents = new PossibleRuleInducerComponents.Builder().build();
        final RuleInductionStoppingConditionChecker stoppingConditionChecker =
                new EvaluationAndCoverageStoppingConditionChecker(
                        EpsilonConsistencyMeasure.getInstance(),
                        EpsilonConsistencyMeasure.getInstance(),
                        EpsilonConsistencyMeasure.getInstance(),
                        ((VCDominanceBasedRoughSetCalculator) unions.getRoughSetCalculator()).getLowerApproximationConsistencyThreshold()
                );
        ruleInducerComponents = new CertainRuleInducerComponents.Builder().
                ruleInductionStoppingConditionChecker(stoppingConditionChecker).
                ruleConditionsPruner(new AttributeOrderRuleConditionsPruner(stoppingConditionChecker)).
                ruleConditionsGeneralizer(new OptimizingRuleConditionsGeneralizer(stoppingConditionChecker)).
                build();

        rules = (new VCDomLEM(ruleInducerComponents, unionAtLeastProvider, unionRuleDecisionsProvider)).generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(""));
        rules.calculateAllCharacteristics();
        RuleSetWithCharacteristics resultSet = rules;

        rules = (new VCDomLEM(ruleInducerComponents, unionAtMostProvider, unionRuleDecisionsProvider)).generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(""));
        rules.calculateAllCharacteristics();
        resultSet = RuleSetWithCharacteristics.join(resultSet, rules);

        resultSet.setLearningInformationTableHash(unions.getInformationTable().getHash());
        //System.out.println("CLASS UNIONS: ");
        //System.out.println(unionAtLeastProvider.getCount());
        /**
        for (int i = 0; i < unionAtLeastProvider.getCount(); i++) {
            System.out.println(unionAtLeastProvider.getApproximatedSet(i) + " "
                    + unionAtLeastProvider.getApproximatedSet(i).getAccuracyOfApproximation() + " "
                    + unionAtLeastProvider.getApproximatedSet(i).getQualityOfApproximation());
        }
        for (int i = 0; i < unionAtMostProvider.getCount(); i++) {
            System.out.println(unionAtMostProvider.getApproximatedSet(i) + " "
                    + unionAtMostProvider.getApproximatedSet(i).getAccuracyOfApproximation() + " "
                    + unionAtMostProvider.getApproximatedSet(i).getQualityOfApproximation());
        }
         */
        //System.out.println("Number of rules: " + resultSet.size());
        //System.out.println("RULES: ");
        //System.out.println(resultSet.serialize());
        int count = 0;
        for (int i = 0; i < informationTable.getNumberOfObjects(); i++) {
            if (resultSet.getRule(0).covers(i, informationTable)){
                count ++;
            };
        }
        rulesNum = resultSet.size();
        for (int i = 0; i < rulesNum; i++) {
            Rules += resultSet.getRule(i) + "\n";
        }
        //System.out.println(count);
        //Unions unions = new Unions(new VCDominanceBasedRoughSetCalculator(RoughMembershipMeasure.getInstance(), 0.1))

        for (int i = 0; i < unionAtLeastProvider.getCount(); i++) {
            unionsS +=(unionAtLeastProvider.getApproximatedSet(i) + " "
                    + unionAtLeastProvider.getApproximatedSet(i).getAccuracyOfApproximation() + " "
                    + unionAtLeastProvider.getApproximatedSet(i).getQualityOfApproximation() + "\n");
        }
        for (int i = 0; i < unionAtMostProvider.getCount(); i++) {
            unionsS +=(unionAtMostProvider.getApproximatedSet(i) + " "
                    + unionAtMostProvider.getApproximatedSet(i).getAccuracyOfApproximation() + " "
                    + unionAtMostProvider.getApproximatedSet(i).getQualityOfApproximation() + "\n");
        }
        resultSetModel = resultSet;

    }

    @Override
    public double classifyInstance(Instance instance) throws Exception {
        //System.out.println(instance);
        for (int i = 0; i < resultSetModel.size(); i++){
            resultSetModel.getRule(i);
        }
        System.out.println(resultSetModel.getRule(0));
        return 1;
    }

    @Override
    public String toString() {

        StringBuffer text = new StringBuffer();

        text.append("\nCLASS UNIONS: \n ===========\n");
        text.append(unionsS);
        //text.append("Number of rules: " + resultSet.size() + "\n");
        //text.append("RULES: ");
        //text.append(resultSet.serialize());
        text.append("\nDRSA rules: \n ===========\n");
        text.append(Rules);
        text.append("\nNum of Rules: " + rulesNum + "\n");
        return text.toString();
    }

    public static void main(String[] args){
       /**
        args = new String[] {
                "-t", "C:\\Users\\AScriet\\Desktop\\data1.arff",
                "-T", "C:\\Users\\AScriet\\Desktop\\data1.arff",
                "-ST", "0.5"
        };
        */
        runClassifier(new DRSA(), args);
    }


}
