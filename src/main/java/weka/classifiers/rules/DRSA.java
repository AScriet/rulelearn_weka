package weka.classifiers.rules;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.enums.*;
import org.enums.RuleType;
import org.rulelearn.approximations.*;
import org.rulelearn.classification.*;
import org.rulelearn.data.*;
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

    public String unionsString = "";
    public RuleType typeOfRules;
    private Double consistencyThreshold;
    protected Boolean advancedVisualization;
    public TypeOfClassifier typeOfClassifier;
    public DefaulClassificationResult defaulClassificationResult;
    private transient Decision[] decisions;
    private transient RuleClassifier classifier;
    private transient EvaluationAttribute[] rlAttributes;
    private transient RuleSetWithCharacteristics resultRulesSet;
    private transient ApproximatedSetProvider unionAtLeastProvider;
    private transient ApproximatedSetProvider unionAtMostProvider;
    protected transient InformationTableWithDecisionDistributions informationTableWithDecisionDistributions;

    public String globalInfo(){

        return "test info about DRSA"
                + getTechnicalInformation().toString();
    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        TechnicalInformation info;
        info = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
        info.setValue(TechnicalInformation.Field.AUTHOR, "Andrii Stepaniuk");
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
        newVector.addElement(new Option(
                "\tSet default classification result\n \t(Default: Majority decision class)\n",
                "ClassifierType",
                0,
                "-DefClassificationResult <type>"
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
        advancedVisualization = Utils.getFlag("advanced-visualization", options);

        String typeOfRules = Utils.getOption("Type", options);
        if (typeOfRules.equals("POSSIBLE")) {
            this.typeOfRules = RuleType.POSSIBLE;
        }else{
            this.typeOfRules = RuleType.CERTAIN;
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
        String defaultClassificationResult1 = Utils.getOption("DefClassificationResult", options);
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

        if (advancedVisualization){
            options.add("-advanced-visualization");
        }

        options.add("-Type");
        options.add(typeOfRules.toString());

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
                new VCDominanceBasedRoughSetCalculator(EpsilonConsistencyMeasure.getInstance(), consistencyThreshold));

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
        if (typeOfRules == RuleType.POSSIBLE) {

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
        else if (typeOfRules == RuleType.CERTAIN) {
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
            unionsString +=(unionAtLeastProvider.getApproximatedSet(i) + " "
                    + unionAtLeastProvider.getApproximatedSet(i).getAccuracyOfApproximation() + " "
                    + unionAtLeastProvider.getApproximatedSet(i).getQualityOfApproximation() + "\n");
        }
        for (int i = 0; i < unionAtMostProvider.getCount(); i++) {
            unionsString +=(unionAtMostProvider.getApproximatedSet(i) + " "
                    + unionAtMostProvider.getApproximatedSet(i).getAccuracyOfApproximation() + " "
                    + unionAtMostProvider.getApproximatedSet(i).getQualityOfApproximation() + "\n");
        }
        prepareClassifier();

    }

    public void prepareClassifier() {
        SimpleDecision simpleDecision;
        SimpleClassificationResult simpleClassificationResult = null;
        SimpleEvaluatedClassificationResult simpleEvaluatedClassificationResult = null;

        switch (defaulClassificationResult) {
            case MAJORITY_DECISION_CLASS:
                List<Decision> modes = informationTableWithDecisionDistributions.getDecisionDistribution().getMode();
                if (modes != null) {
                    simpleDecision = (SimpleDecision)modes.get(0);
                } else {
                    simpleDecision = new SimpleDecision(new UnknownSimpleFieldMV2(), 0);
                }
                if (typeOfClassifier == TypeOfClassifier.SIMPLE_RULE_CLASSIFIER_AVG || typeOfClassifier == TypeOfClassifier.SIMPLE_RULE_CLASSIFIER_MODE) {
                    simpleClassificationResult = new SimpleClassificationResult(simpleDecision);
                }
                else{
                    simpleEvaluatedClassificationResult = new SimpleEvaluatedClassificationResult(simpleDecision, 1.0);
                }
            case MEDIAN_DECISION_CLASS:
                Decision median = informationTableWithDecisionDistributions.getDecisionDistribution().getMedian(informationTableWithDecisionDistributions.getOrderedUniqueFullyDeterminedDecisions());

                if (median != null) {
                    simpleDecision = (SimpleDecision)median;
                } else {
                    simpleDecision = new SimpleDecision(new UnknownSimpleFieldMV2(), 0);
                }
                if (typeOfClassifier == TypeOfClassifier.SIMPLE_RULE_CLASSIFIER_AVG || typeOfClassifier == TypeOfClassifier.SIMPLE_RULE_CLASSIFIER_MODE) {
                    simpleClassificationResult = new SimpleClassificationResult(simpleDecision);
                }
                else {
                    simpleEvaluatedClassificationResult = new SimpleEvaluatedClassificationResult(simpleDecision, 1.0);
                }
                break;
        }

        switch (typeOfClassifier) {
            case SIMPLE_RULE_CLASSIFIER_AVG:
                classifier = new SimpleRuleClassifier(resultRulesSet, simpleClassificationResult);
                break;
            case SIMPLE_RULE_CLASSIFIER_MODE:
                classifier = new SimpleOptimizingCountingRuleClassifier(resultRulesSet, simpleClassificationResult, informationTableWithDecisionDistributions);
                break;
            case SCORING_RULE_CLASSIFIER:
                classifier = new ScoringRuleClassifier(resultRulesSet, simpleEvaluatedClassificationResult, ScoringRuleClassifier.Mode.SCORE, informationTableWithDecisionDistributions);
                break;
            case HYBRID_SCORING_RULE_CLASSIFIER:
                classifier = new ScoringRuleClassifier(resultRulesSet, simpleEvaluatedClassificationResult, ScoringRuleClassifier.Mode.HYBRID, informationTableWithDecisionDistributions);
                break;
            default:
                break;
        }
    }
    @Override
    public double classifyInstance(Instance instance) {
        double covers = 0.0;

        ArffInstance2Table arffInstance2Table = new ArffInstance2Table();
        InformationTable informationTable = arffInstance2Table.getTable(rlAttributes, instance);

        IntArrayList indicesOfCoveringRules = new IntArrayList();
        ClassificationResult classificationResults = classifier.classify(0, informationTable, indicesOfCoveringRules);

        for (int i = 0; i < decisions.length; ++i) {
            if (decisions[i].equals(classificationResults.getSuggestedDecision())) {
                covers = i;
            }
        }
        return covers;
    }

    @Override
    public String toString() {
        if (advancedVisualization) {
            Visualization.run(unionAtLeastProvider, unionAtMostProvider, informationTableWithDecisionDistributions, resultRulesSet);
            return "\nDRSA rules: \n ===========\n" +
                    resultRulesSet.serialize("\n");
        }
        else {
            return "\nCLASS UNIONS: \n ===========\n" +
                    unionsString +
                    "\nDRSA rules: \n ===========\n" +
                    resultRulesSet.serialize("\n") +
                    "\nNum of Rules: " + resultRulesSet.size() + "\n";
        }
    }

    public Double getConsistencyThreshold() {return this.consistencyThreshold;}
    public void setConsistencyThreshold(Double consistencyThreshold) {this.consistencyThreshold = consistencyThreshold;}
    public boolean getAdvancedVisualization() {return this.advancedVisualization;}
    public void setAdvancedVisualization(boolean advancedVisualization) {this.advancedVisualization = advancedVisualization;}
    public RuleType getTypeOfRules() {return this.typeOfRules;}
    public void setTypeOfRules(RuleType typeOfRules) {this.typeOfRules = typeOfRules;}
    public DefaulClassificationResult getDefaulClassificationResult() {return this.defaulClassificationResult;};
    public void setDefaulClassificationResult(DefaulClassificationResult defaulClassificationResult){this.defaulClassificationResult = defaulClassificationResult;}
    public TypeOfClassifier getTypeOfClassifier(){return this.typeOfClassifier;}
    public void setTypeOfClassifier(TypeOfClassifier typeOfClassifier){ this.typeOfClassifier = typeOfClassifier;}

    public void resetOptions(){
        this.consistencyThreshold = 0.0;
        this.typeOfRules = RuleType.CERTAIN;
        this.typeOfClassifier = TypeOfClassifier.SIMPLE_RULE_CLASSIFIER_AVG;
        this.defaulClassificationResult = DefaulClassificationResult.MAJORITY_DECISION_CLASS;
        this.advancedVisualization = false;
    }

    public DRSA(){
        resetOptions();
    }

    public static void main(String[] args){
        runClassifier(new DRSA(), args);
    }


}
