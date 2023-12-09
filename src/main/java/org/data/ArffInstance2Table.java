package org.data;

import org.rulelearn.core.InvalidTypeException;
import org.rulelearn.data.*;
import org.rulelearn.types.*;
import weka.core.Instances;
import weka.core.Attribute;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class ArffInstance2Table {
    public EvaluationAttribute[] getAttributes(Instances instances){
        Attribute attribute;
        String attributeName;
        boolean isActive = false;
        boolean isInteger = false;
        int numInstances = instances.numInstances();
        AttributeType attributeType = AttributeType.CONDITION;
        Enumeration<Object> nominalValuesEnumeration;
        List<String> nominalValues;
        UnknownSimpleField missingValueType;
        AttributePreferenceType attributePreferenceType;
        List<EvaluationAttribute> rLAttributesList = new ArrayList<EvaluationAttribute>();
        int attributeNum = instances.numAttributes();




        List<Integer> ruleLearnAttributeIndex2WekaAttributeIndex = new ArrayList<Integer>();

        for (int i = 0; i < attributeNum; i++) {

            attributeType = AttributeType.DECISION;
            missingValueType = UnknownSimpleFieldMV2.getInstance();
            isActive = false;

            attribute = instances.attribute(i);
            attributeName = attribute.name();

            if (attributeName.contains("[mv1.5]")) {
                missingValueType = UnknownSimpleFieldMV15.getInstance();
            } else {
                missingValueType = UnknownSimpleFieldMV2.getInstance();
            }
            if (attributeName.contains("[g]")) {
                attributePreferenceType = AttributePreferenceType.GAIN;
            } else if (attributeName.contains("[c]")) {
                attributePreferenceType = AttributePreferenceType.COST;
            } else {
                attributePreferenceType = AttributePreferenceType.NONE;
            }

            isActive = attributeName.contains("[a]");

            if (i < attributeNum - 1) {
                attributeType = AttributeType.CONDITION;
            }

            if (attributeName.contains("[")) {
                int nameEnd = attributeName.indexOf("[");
                attributeName = attributeName.substring(0, nameEnd);
            }
            switch (attribute.type()) {
                case Attribute.NUMERIC:
                    isInteger = attributeName.contains("[i]");

                    if (isInteger) {
                        rLAttributesList.add(new EvaluationAttribute(
                                attributeName,
                                isActive,
                                attributeType,
                                IntegerFieldFactory.getInstance().create(0, attributePreferenceType),
                                missingValueType,
                                attributePreferenceType
                        ));
                    } else {
                        rLAttributesList.add(new EvaluationAttribute(
                                attributeName,
                                isActive,
                                attributeType,
                                RealFieldFactory.getInstance().create(0.0, attributePreferenceType),
                                missingValueType,
                                attributePreferenceType
                        ));
                    }

                    break;
                case Attribute.NOMINAL:
                    nominalValuesEnumeration = attribute.enumerateValues();
                    nominalValues = new ArrayList<>();

                    while (nominalValuesEnumeration.hasMoreElements()) {
                        nominalValues.add((String) nominalValuesEnumeration.nextElement());
                    }

                    try {
                        rLAttributesList.add(new EvaluationAttribute(
                                attributeName,
                                isActive,
                                attributeType,
                                EnumerationFieldFactory.getInstance().create(new ElementList(nominalValues.toArray(new String[0])), 0, attributePreferenceType),
                                missingValueType,
                                attributePreferenceType));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        return null; //this should not happen
                    }
                    break;
                default:
                    throw new InvalidTypeException("Unsupported WEKA's attribute type.");

            }
            ruleLearnAttributeIndex2WekaAttributeIndex.add(i);
        }

        EvaluationAttribute[] rLAttributes = rLAttributesList.toArray(new EvaluationAttribute[0]);
        int numRuleLearnAttributes = rLAttributes.length;

        return rLAttributes;
    }
    public InformationTable convert(Instances instances){

        Attribute attribute;
        String attributeName;
        boolean isActive = false;
        boolean isInteger = false;
        int numInstances = instances.numInstances();
        AttributeType attributeType = AttributeType.CONDITION;
        Enumeration<Object> nominalValuesEnumeration;
        List<String> nominalValues;
        UnknownSimpleField missingValueType;
        AttributePreferenceType attributePreferenceType;
        List<EvaluationAttribute> rLAttributesList = new ArrayList<EvaluationAttribute>();
        int attributeNum = instances.numAttributes();




        List<Integer> ruleLearnAttributeIndex2WekaAttributeIndex = new ArrayList<Integer>();

        for (int i = 0; i < attributeNum; i++) {

            attributeType = AttributeType.DECISION;
            missingValueType = UnknownSimpleFieldMV2.getInstance();
            isActive = false;

            attribute = instances.attribute(i);
            attributeName = attribute.name();

            if (attributeName.contains("[mv1.5]")) {
                missingValueType = UnknownSimpleFieldMV15.getInstance();
            } else {
                missingValueType = UnknownSimpleFieldMV2.getInstance();
            }
            if (attributeName.contains("[g]")) {
                attributePreferenceType = AttributePreferenceType.GAIN;
            } else if (attributeName.contains("[c]")) {
                attributePreferenceType = AttributePreferenceType.COST;
            } else {
                attributePreferenceType = AttributePreferenceType.NONE;
            }

            isActive = attributeName.contains("[a]");

            if (i < attributeNum - 1) {
                attributeType = AttributeType.CONDITION;
            }

            if (attributeName.contains("[")) {
                int nameEnd = attributeName.indexOf("[");
                attributeName = attributeName.substring(0, nameEnd);
            }
            switch (attribute.type()) {
                case Attribute.NUMERIC:
                    isInteger = attributeName.contains("[i]");

                    if (isInteger) {
                        rLAttributesList.add(new EvaluationAttribute(
                                attributeName,
                                isActive,
                                attributeType,
                                IntegerFieldFactory.getInstance().create(0, attributePreferenceType),
                                missingValueType,
                                attributePreferenceType
                        ));
                    } else {
                        rLAttributesList.add(new EvaluationAttribute(
                                attributeName,
                                isActive,
                                attributeType,
                                RealFieldFactory.getInstance().create(0.0, attributePreferenceType),
                                missingValueType,
                                attributePreferenceType
                        ));
                    }

                    break;
                case Attribute.NOMINAL:
                    nominalValuesEnumeration = attribute.enumerateValues();
                    nominalValues = new ArrayList<>();

                    while (nominalValuesEnumeration.hasMoreElements()) {
                        nominalValues.add((String) nominalValuesEnumeration.nextElement());
                    }

                    try {
                        rLAttributesList.add(new EvaluationAttribute(
                                attributeName,
                                isActive,
                                attributeType,
                                EnumerationFieldFactory.getInstance().create(new ElementList(nominalValues.toArray(new String[0])), 0, attributePreferenceType),
                                missingValueType,
                                attributePreferenceType));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        return null; //this should not happen
                    }
                    break;

                default:
                    throw new InvalidTypeException("Unsupported WEKA's attribute type.");

            }
            ruleLearnAttributeIndex2WekaAttributeIndex.add(i);
        }

        EvaluationAttribute[] rLAttributes = rLAttributesList.toArray(new EvaluationAttribute[0]);
        int numRuleLearnAttributes = rLAttributes.length;

        EvaluationField[] fields;
        List<Field[]> listOfFields = new ArrayList<Field[]>();
        double value; //in WEKA numeric and nominal values are internally stored as doubles

        for (int i = 0; i < numInstances; i ++) {
            fields = new EvaluationField[numRuleLearnAttributes];
            for (int j = 0; j < numRuleLearnAttributes; j++) {
                if (instances.instance(i).isMissing(ruleLearnAttributeIndex2WekaAttributeIndex.get(j))) {
                    fields[j] =rLAttributes[j].getMissingValueType();
                }
                else {
                    value = instances.instance(i).value(ruleLearnAttributeIndex2WekaAttributeIndex.get(j));
                    if (rLAttributes[j].getValueType() instanceof IntegerField) {
                        fields[j] = IntegerFieldFactory.getInstance().create((int)value, rLAttributes[j].getPreferenceType());
                    } else if (rLAttributes[j].getValueType() instanceof  RealField) {
                        fields[j] = RealFieldFactory.getInstance().create((int)value, rLAttributes[j].getPreferenceType());
                    } else if (rLAttributes[j].getValueType() instanceof EnumerationField) {
                        fields[j] = EnumerationFieldFactory.getInstance().create(((EnumerationField)rLAttributes[j].getValueType()).getElementList(), (int)value, rLAttributes[j].getPreferenceType());
                    }
                }

            }
            listOfFields.add(fields);

        }

        return new InformationTable(rLAttributes, listOfFields);
    }
}
