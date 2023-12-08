package weka.classifiers.rules;

import org.rulelearn.approximations.*;
import org.rulelearn.data.InformationTable;
import org.rulelearn.data.InformationTableWithDecisionDistributions;
import org.rulelearn.rules.Rule;
import org.rulelearn.rules.RuleSetWithCharacteristics;
import org.rulelearn.measures.dominance.EpsilonConsistencyMeasure;
import org.rulelearn.rules.*;
import org.data.ArffConverter;
import org.data.ArffInstance2Table;
import weka.classifiers.AbstractClassifier;
import weka.core.*;
import org.rulelearn.approximations.VCDominanceBasedRoughSetCalculator;

import java.util.*;

public class DRSA extends AbstractClassifier implements
        TechnicalInformationHandler {

    String unionsS = "";
    private double Th = 0.0;
    protected int instanceNum = 0;
    protected int batchSize = 0;
    private ArrayList<String> decisions;
    protected transient RuleSetWithCharacteristics resultSetModel = null;
    protected transient InformationTableWithDecisionDistributions informationTableWithDecisionDistributions = null;
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
        ArffInstance2Table arffC = new ArffInstance2Table();

        informationTable = arffC.convert(data);

        batchSize = data.size();
        /* enum class values */
        int n = data.numClasses();
        decisions = new ArrayList<String>();
        for (int i = 0; i < n; i++) {
            decisions.add(data.classAttribute().value(i));
        }

        informationTableWithDecisionDistributions = new InformationTableWithDecisionDistributions(informationTable);

        RuleSetWithComputableCharacteristics rules;
        //UnionsWithSingleLimitingDecision unions = new UnionsWithSingleLimitingDecision(
        //        informationTableWithDecisionDistributions,
        //        new VCDominanceBasedRoughSetCalculator(RoughMembershipMeasure.getInstance(), 0.0));

        UnionsWithSingleLimitingDecision unions;



        unions = new UnionsWithSingleLimitingDecision(
                informationTableWithDecisionDistributions,
                new VCDominanceBasedRoughSetCalculator(EpsilonConsistencyMeasure.getInstance(), Th));

        RuleInducerComponents ruleInducerComponents;

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
        String ruleStr;
        double covers = 1;
        for (int i = 0; i < resultSetModel.size(); i++) {
            Rule rule = resultSetModel.getRule(i);
            if(rule.covers(instanceNum, informationTableWithDecisionDistributions)) {
                ruleStr = rule.getDecision().toString();
                for (int j = 0; j < instance.numClasses(); j++){
                    if (ruleStr.contains(decisions.get(j))){
                        covers = j;
                    }
                }
                break;
            }

        }
        instanceNum += 1;
        if (instanceNum == batchSize)
            instanceNum = 0;

        return covers;
    }


    @Override
    public String toString() {

        StringBuffer text = new StringBuffer();

        text.append("\nCLASS UNIONS: \n ===========\n");
        text.append(unionsS);
        text.append("\nDRSA rules: \n ===========\n");
        text.append(resultSetModel.serialize("\n"));
        text.append("\nNum of Rules: " + resultSetModel.size() + "\n");
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
