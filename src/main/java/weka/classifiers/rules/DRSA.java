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
        TechnicalInformationHandler, OptionHandler {

    String unionsS = "";
    private double ConsistencyThreshold = 0.0;
    private Boolean AdvancedVisualization = false;
    private transient RuleType TypeOfRules = RuleType.CERTAIN;
    private transient Decision[] decisions;
    private transient EvaluationAttribute[] rlAttributes;
    private transient RuleSetWithCharacteristics resultRulesSet = null;
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
        newVector.addElement(new Option (
                "\tSet type of rules\n \t(Default: certain)\n",
                "Type",
                0,
                "-Type <type>"
        ));
        newVector.addAll(Collections.list(super.listOptions()));
        return newVector.elements();
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        String conThresh = Utils.getOption("ST", options);
        if (!conThresh.isEmpty()) {
            setConsistencyThreshold(Double.parseDouble(conThresh));
        } else {
            setConsistencyThreshold(0.0);
        }
        AdvancedVisualization = Utils.getFlag("advanced-visualization", options);

        String typeOfRules = Utils.getOption("Type", options);
        if (typeOfRules.equals("possible")) {
            TypeOfRules = RuleType.POSSIBLE;
        }else{
            TypeOfRules = RuleType.CERTAIN;
        }


        super.setOptions(options);

        Utils.checkForRemainingOptions(options);
    }

    @Override
    public String[] getOptions() {
        Vector<String> options = new Vector<String>();
        options.add("-ST");
        options.add(String.valueOf(getConsistencyThreshold()));

        if (AdvancedVisualization){
            options.add("-advanced-visualization");
        }

        options.add("-Type");
        options.add(TypeOfRules.toString().toLowerCase());

        Collections.addAll(options, super.getOptions());

        return options.toArray(new String[0]);
    }

    @Override
    public void buildClassifier(Instances data) throws Exception {
        InformationTable informationTable;
        ArffInstance2Table ArffConverter = new ArffInstance2Table();
        informationTable = ArffConverter.convert(data);

        rlAttributes = ArffConverter.getAttributes(data);

        informationTableWithDecisionDistributions = new InformationTableWithDecisionDistributions(informationTable);

        UnionsWithSingleLimitingDecision unions;
        unions = new UnionsWithSingleLimitingDecision(
                informationTableWithDecisionDistributions,
                new VCDominanceBasedRoughSetCalculator(EpsilonConsistencyMeasure.getInstance(), ConsistencyThreshold));

        ApproximatedSetProvider unionAtLeastProvider = new UnionProvider(Union.UnionType.AT_LEAST, unions);
        ApproximatedSetProvider unionAtMostProvider = new UnionProvider(Union.UnionType.AT_MOST, unions);
        ApproximatedSetRuleDecisionsProvider unionRuleDecisionsProvider = new UnionWithSingleLimitingDecisionRuleDecisionsProvider();

        int n = data.numClasses();
        decisions = new Decision[n];
        decisions = informationTableWithDecisionDistributions.getOrderedUniqueFullyDeterminedDecisions();
        if (data.classAttribute().toString().contains("[c]")){
            for (int i = 0; i < n / 2; i++){
                Decision temp = decisions[i];
                decisions[i] = decisions[n - i - 1];
                decisions[decisions.length - i - 1] = temp;
            }

        }
        RuleSetWithComputableCharacteristics rules;
        RuleInducerComponents ruleInducerComponents = null;
        if (TypeOfRules == RuleType.POSSIBLE) {

            if (!unions.getInformationTable().isSuitableForInductionOfPossibleRules()) {
                throw new Exception("Creating possible rules is not possible - learning data contain missing attribute values that can lead to non-transitivity of dominance/indiscernibility relation.");
            }

            ruleInducerComponents = new PossibleRuleInducerComponents.Builder().build();

            rules = (new VCDomLEM(ruleInducerComponents, unionAtLeastProvider, unionRuleDecisionsProvider)).
                    generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(""));
            rules.calculateAllCharacteristics();
            resultRulesSet = rules;

            rules = (new VCDomLEM(ruleInducerComponents, unionAtMostProvider, unionRuleDecisionsProvider)).
                    generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(""));
            rules.calculateAllCharacteristics();
            resultRulesSet = RuleSetWithCharacteristics.join(resultRulesSet, rules);
        }
        if (TypeOfRules == RuleType.CERTAIN) {
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

            rules = (new VCDomLEM(ruleInducerComponents, unionAtLeastProvider, unionRuleDecisionsProvider)).
                    generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(""));
            rules.calculateAllCharacteristics();
            resultRulesSet = rules;

            rules = (new VCDomLEM(ruleInducerComponents, unionAtMostProvider, unionRuleDecisionsProvider)).
                    generateAndFilterRules(CompositeRuleCharacteristicsFilter.of(""));
            rules.calculateAllCharacteristics();
            resultRulesSet = RuleSetWithCharacteristics.join(resultRulesSet, rules);

            resultRulesSet.setLearningInformationTableHash(unions.getInformationTable().getHash());
        }

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
        if (AdvancedVisualization) {
            Visualization.run(unionAtLeastProvider, unionAtMostProvider, informationTableWithDecisionDistributions, resultRulesSet);
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
    public double classifyInstance(Instance instance) {
        double covers = 0;
        for (int i = 0; i < resultRulesSet.size(); i++) {
            Rule rule = resultRulesSet.getRule(i);
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

        return "\nCLASS UNIONS: \n ===========\n" +
                unionsS +
                "\nDRSA rules: \n ===========\n" +
                resultRulesSet.serialize("\n") +
                "\nNum of Rules: " + resultRulesSet.size() + "\n";
    }

    public double getConsistencyThreshold() {
        return ConsistencyThreshold;
    }
    public void setConsistencyThreshold(double consistencyThreshold) {
        ConsistencyThreshold = consistencyThreshold;
    }
    public boolean getAdvancedVisualization() {
        return AdvancedVisualization;
    }
    public void setAdvancedVisualization(boolean advancedVisualization) {
        AdvancedVisualization = advancedVisualization;
    }
    public RuleType getTypeOfRules() {
        return  TypeOfRules;
    }
    public void setTypeOfRules(RuleType typeOfRules) {
        TypeOfRules = typeOfRules;
    }




    public static void main(String[] args){
        runClassifier(new DRSA(), args);
    }


}
