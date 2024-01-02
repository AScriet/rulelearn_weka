package weka.classifiers.rules;

import org.rulelearn.approximations.*;
import org.rulelearn.data.Decision;
import org.rulelearn.data.EvaluationAttribute;
import org.rulelearn.data.InformationTable;
import org.rulelearn.data.InformationTableWithDecisionDistributions;
import org.rulelearn.rules.Rule;
import org.rulelearn.rules.RuleSetWithCharacteristics;
import org.rulelearn.measures.dominance.EpsilonConsistencyMeasure;
import org.rulelearn.rules.*;
import org.data.ArffInstance2Table;
import org.rulelearn.types.*;
import weka.classifiers.AbstractClassifier;
import weka.core.*;
import org.rulelearn.approximations.VCDominanceBasedRoughSetCalculator;
import weka.gui.Visualization;

import java.util.*;

public class DRSA extends AbstractClassifier implements
        TechnicalInformationHandler {

    String unionsS = "";
    private double Th = 0.0;
    private Boolean AdvancedVisualization = false;
    protected int instanceNum = 0;
    private transient Decision[] decisions;
    protected transient EvaluationAttribute[] rlAttributes;
    protected transient RuleSetWithCharacteristics resultSetModel = null;
    protected transient InformationTableWithDecisionDistributions informationTableWithDecisionDistributions = null;

    public String globalInfo(){

        return "test info about DRSA"
                + getTechnicalInformation().toString();
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        TechnicalInformation info;
        info = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
        info.setValue(TechnicalInformation.Field.AUTHOR, "X");
        return info;
    }

    @Override
    public Enumeration<Option> listOptions() {
        Vector<Option> newVector = new Vector<Option>(1);
        newVector.addElement(new Option(
                "\tSet consistency threshold\n \t(Default: 0.0)\n",
                "ST",
                0,
                "-ST <consistency threshold>"));
        newVector.addElement(new Option(
                "\tShow advanced visualization\n \t(Default: off)\n",
                "-advanced-visualization",
                0,
                "-advanced-visualization"));

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
        AdvancedVisualization = Utils.getFlag("advanced-visualization", options);


        super.setOptions(options);

        Utils.checkForRemainingOptions(options);
    }

    @Override
    public String[] getOptions() {
        Vector<String> options = new Vector<String>();
        options.add("-ST");
        options.add("" + Th);

        if (AdvancedVisualization){
            options.add("-advanced-visualization");
        }
        Collections.addAll(options, super.getOptions());

        return options.toArray(new String[0]);
    }

    @Override
    public void buildClassifier(Instances data) throws Exception {


        InformationTable informationTable;
        ArffInstance2Table arffC = new ArffInstance2Table();
        informationTable = arffC.convert(data);

        rlAttributes = arffC.getAttributes(data);

        /* enum class values */
        int n = data.numClasses();


        informationTableWithDecisionDistributions = new InformationTableWithDecisionDistributions(informationTable);
        RuleSetWithComputableCharacteristics rules;
        //UnionsWithSingleLimitingDecision unions = new UnionsWithSingleLimitingDecision(
        //        informationTableWithDecisionDistributions,
        //        new VCDominanceBasedRoughSetCalculator(RoughMembershipMeasure.getInstance(), 0.0));

        UnionsWithSingleLimitingDecision unions;

        decisions = new Decision[n];
        decisions = informationTableWithDecisionDistributions.getOrderedUniqueFullyDeterminedDecisions();

        if (data.classAttribute().toString().contains("[c]")){
            for (int i = 0; i < n / 2; i++){
                Decision temp = decisions[i];
                decisions[i] = decisions[n - i - 1];
                decisions[decisions.length - i - 1] = temp;
            }

        }


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
        ;
        resultSetModel = resultSet;
        if (AdvancedVisualization) {
            Visualization frame = new Visualization();
            frame.run(resultSetModel.serialize("\n"), unionAtLeastProvider, unionAtMostProvider, informationTableWithDecisionDistributions);
        }
    }
    public boolean cover(Rule rule, Instance instance){

        int conditionsSize = rule.getConditions().length;

        EvaluationField[] fields;
        int numRuleLearnAttributes = rlAttributes.length;
        fields = new EvaluationField[numRuleLearnAttributes];
        double value;

        for (int i = 0; i < numRuleLearnAttributes; i++){
            if (instance.isMissing(i)){
                fields[i] = rlAttributes[i].getMissingValueType();
            }
            else {
                value = instance.value(i);
                if (rlAttributes[i].getValueType() instanceof IntegerField){
                    fields[i] = IntegerFieldFactory.getInstance().create((int)value, rlAttributes[i].getPreferenceType());
                }
                else if (rlAttributes[i].getValueType() instanceof  RealField) {
                    fields[i] = RealFieldFactory.getInstance().create((int)value, rlAttributes[i].getPreferenceType());
                } else if (rlAttributes[i].getValueType() instanceof EnumerationField) {
                    fields[i] = EnumerationFieldFactory.getInstance().create(((EnumerationField)rlAttributes[i].getValueType()).getElementList(), (int)value, rlAttributes[i].getPreferenceType());
                }
            }
        }

        for (int i = 0; i < conditionsSize; i++) {

            int index = rule.getConditions()[i].getAttributeWithContext().getAttributeIndex();

            for (int j = 0; j < instance.numAttributes() - 1; j++) {
                        if (!rule.getConditions()[i].satisfiedBy(fields[index]))
                            return false;
                    }
            }
        return true;
    }
    @Override
    public double classifyInstance(Instance instance) throws Exception {
        double covers = 0;
        for (int i = 0; i < resultSetModel.size(); i++) {
            Rule rule = resultSetModel.getRule(i);
            //if(rule.covers(instanceNum, informationTableWithDecisionDistributions)) {
            if(cover(rule, instance)) {
                for (int j = 0; j < instance.numClasses(); j++){
                    //System.out.println(rule.getDecision().toString() + " " + decisions[j].serialize());
                    if (rule.getDecision().toString().contains(decisions[j].serialize().replace("8:", ""))){
                        covers = j;
                    }
                }
                break;
            }
        }

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
        runClassifier(new DRSA(), args);
    }


}
