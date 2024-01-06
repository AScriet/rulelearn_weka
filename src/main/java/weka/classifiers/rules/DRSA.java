package weka.classifiers.rules;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.enums.*;
import org.enums.RuleType;
import org.rulelearn.approximations.*;
import org.rulelearn.classification.*;
import org.rulelearn.data.*;
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
        TechnicalInformationHandler{

    String unionsS = "";
    private Double ConsistencyThreshold;
    protected boolean AdvancedVisualization;
    public TypeOfClassifier typeOfClassifier;
    public DefaulClassificationResult defaulClassificationResult;
    public RuleType TypeOfRules;
    private transient Decision[] decisions;
    private transient EvaluationAttribute[] rlAttributes;
    private transient ApproximatedSetProvider unionAtLeastProvider = null;
    private transient ApproximatedSetProvider unionAtMostProvider = null;
    public transient RuleSetWithCharacteristics resultRulesSet;
    protected transient InformationTableWithDecisionDistributions informationTableWithDecisionDistributions = null;
    private transient SimpleClassificationResult simpleClassificationResult;
    private transient RuleClassifier classifier = null;

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
                "-ST <consistency threshold>"
        ));
        newVector.addElement(new Option(
                "\tShow advanced visualization\n \t(Default: off)\n",
                "-advanced-visualization",
                0,
                "-advanced-visualization"
        ));
        newVector.addElement(new Option (
                "\tSet type of rules\n \t(Default: certain)\n",
                "Type",
                0,
                "-Type <type>"
        ));
        newVector.addElement(new Option(
                "\tSet type of classifier\n \t(Default: Simple Rule Classifier (mode))\n",
                "ClassifierType",
                0,
                "-ClassifierType <type>"
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
        if (typeOfRules.equals("POSSIBLE")) {
            TypeOfRules = RuleType.POSSIBLE;
        }else{
            TypeOfRules = RuleType.CERTAIN;
        }
        String classificationType = Utils.getOption("ClassificationType", options);
        switch (classificationType) {
            case "SIMPLE_RULE_CLASSIFIER_AVG":
                typeOfClassifier = TypeOfClassifier.SIMPLE_RULE_CLASSIFIER_AVG;
                break;
            case "SIMPLE_RULE_CLASSIFIER_MODE":
                typeOfClassifier = TypeOfClassifier.SIMPLE_RULE_CLASSIFIER_MODE;
                break;
            case "SCORING_RULE_CLASSIFIER":
                typeOfClassifier = TypeOfClassifier.SCORING_RULE_CLASSIFIER;
                break;
            case "HYBRID_SCORING_RULE_CLASSIFIER":
                typeOfClassifier = TypeOfClassifier.HYBRID_SCORING_RULE_CLASSIFIER;
                break;
        }
        String defaultClassificationResult1 = Utils.getOption("DefaulClassificationResult", options);
        if (defaultClassificationResult1.equals("MAJORITY_DECISION_CLASS")) {
            defaulClassificationResult = DefaulClassificationResult.MAJORITY_DECISION_CLASS;
        }else{
            defaulClassificationResult = DefaulClassificationResult.MEDIAN_DECISION_CLASS;
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
        options.add(TypeOfRules.toString());

        options.add("-ClassificationType");
        options.add(typeOfClassifier.toString());

        options.add("-DefaultClassificationResult");
        options.add(defaulClassificationResult.toString());

        Collections.addAll(options, super.getOptions());

        return options.toArray(new String[0]);
    }

    @Override
    public void buildClassifier(Instances data) throws Exception {
        InformationTable informationTable;
        ArffInstance2Table ArffConverter = new ArffInstance2Table();
        rlAttributes = ArffConverter.getAttributes(data);
        informationTable = ArffConverter.getTable(rlAttributes, data);

        rlAttributes = ArffConverter.getAttributes(data);

        informationTableWithDecisionDistributions = new InformationTableWithDecisionDistributions(informationTable);

        UnionsWithSingleLimitingDecision unions;
        unions = new UnionsWithSingleLimitingDecision(
                informationTableWithDecisionDistributions,
                new VCDominanceBasedRoughSetCalculator(EpsilonConsistencyMeasure.getInstance(), ConsistencyThreshold));

        unionAtLeastProvider = new UnionProvider(Union.UnionType.AT_LEAST, unions);
        unionAtMostProvider = new UnionProvider(Union.UnionType.AT_MOST, unions);
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
        else if (TypeOfRules == RuleType.CERTAIN) {
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
        SimpleDecision simpleDecision = null;
        switch (defaulClassificationResult) {
            case MAJORITY_DECISION_CLASS:
                List<Decision> modes = informationTableWithDecisionDistributions.getDecisionDistribution().getMode();
                if (modes != null) {
                    simpleDecision = (SimpleDecision)modes.get(0);
                } else {
                    simpleDecision = new SimpleDecision(new UnknownSimpleFieldMV2(), 0);
                }
                simpleClassificationResult = new SimpleClassificationResult(simpleDecision);
            case MEDIAN_DECISION_CLASS:
                Decision median = informationTableWithDecisionDistributions.getDecisionDistribution().getMedian(informationTableWithDecisionDistributions.getOrderedUniqueFullyDeterminedDecisions());

                if (median != null) {
                    simpleDecision = (SimpleDecision)median;
                } else {
                    simpleDecision = new SimpleDecision(new UnknownSimpleFieldMV2(), 0);
                }

                simpleClassificationResult = new SimpleClassificationResult(simpleDecision);
                break;
        }

        switch (typeOfClassifier) {
            case SIMPLE_RULE_CLASSIFIER_AVG:
                classifier = new SimpleRuleClassifier(resultRulesSet, simpleClassificationResult);
                break;
            case SIMPLE_RULE_CLASSIFIER_MODE:
                classifier = new SimpleOptimizingCountingRuleClassifier(resultRulesSet, simpleClassificationResult, informationTable);
                break;
            default:
                break;
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
/*
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
*/
        ArffInstance2Table arffInstance2Table = new ArffInstance2Table();
        InformationTable informationTable = arffInstance2Table.getTable(rlAttributes, instance);

        covers = classify(informationTable);
        return covers;
    }

    public double classify(InformationTable informationTable) {


        double covers = 0.0;

        int objectIndex;
        int objectCount = informationTable.getNumberOfObjects();
        IntList[] indicesOfCoveringRules = new IntList[objectCount];

        ClassificationResult[] classificationResults = new ClassificationResult[objectCount];
        for (objectIndex = 0; objectIndex < classificationResults.length; objectIndex++) {
            indicesOfCoveringRules[objectIndex] = new IntArrayList();
            classificationResults[objectIndex] = classifier.classify(objectIndex, informationTable, indicesOfCoveringRules[objectIndex]);
        }
        //System.out.println(classificationResults[0].getSuggestedDecision());
        for (int i = 0; i < decisions.length; ++i) {
            if (decisions[i].serialize().toString().contains(classificationResults[0].getSuggestedDecision().serialize().replace("8:", ""))) {
                covers = i;
            }
        }
        return covers;
    }

    @Override
    public String toString() {
        if (AdvancedVisualization) {
            Visualization.run(unionAtLeastProvider, unionAtMostProvider, informationTableWithDecisionDistributions, resultRulesSet);
        }
        return "\nCLASS UNIONS: \n ===========\n" +
                unionsS +
                "\nDRSA rules: \n ===========\n" +
                resultRulesSet.serialize("\n") +
                "\nNum of Rules: " + resultRulesSet.size() + "\n";
    }

    public Double getConsistencyThreshold() {return ConsistencyThreshold;}
    public void setConsistencyThreshold(Double consistencyThreshold) {this.ConsistencyThreshold = consistencyThreshold;}
    public boolean getAdvancedVisualization() {return AdvancedVisualization;}
    public void setAdvancedVisualization(boolean advancedVisualization) {this.AdvancedVisualization = advancedVisualization;}
    public RuleType getTypeOfRules() {return  TypeOfRules;}
    public void setTypeOfRules(RuleType typeOfRules) {this.TypeOfRules = typeOfRules;}
    public DefaulClassificationResult getDefaulClassificationResult() {return defaulClassificationResult;};
    public void setDefaulClassificationResult(DefaulClassificationResult defaulClassificationResult){this.defaulClassificationResult = defaulClassificationResult;}
    public TypeOfClassifier getTypeOfClassifier(){return typeOfClassifier;}
    public void setTypeOfClassifier(TypeOfClassifier typeOfClassifier){ this.typeOfClassifier = typeOfClassifier;}

    public void resetOptions(){
        this.ConsistencyThreshold = 0.0;
        this.TypeOfRules = RuleType.CERTAIN;
        this.typeOfClassifier = TypeOfClassifier.SIMPLE_RULE_CLASSIFIER_MODE;
        this.defaulClassificationResult = DefaulClassificationResult.MAJORITY_DECISION_CLASS;
        this.AdvancedVisualization = false;
    }

    public DRSA(){
        resetOptions();
    }


    public static void main(String[] args){
        runClassifier(new DRSA(), args);
    }


}
