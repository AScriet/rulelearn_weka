package tools;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.rulelearn.data.*;
import org.rulelearn.data.arff.ArffReader;
import org.rulelearn.types.*;

import java.lang.annotation.ElementType;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;

public class ArffConverter {
    private class MinimalAttribute {
        private String name;
        private EvaluationField valueType;
        private UnknownSimpleField unknownSimpleField;
        private AttributePreferenceType attributePreferenceType;

        private MinimalAttribute(String name, EvaluationField valueType,
                                 UnknownSimpleField unknownSimpleField, AttributePreferenceType attributePreferenceType) {
            this.name = name;
            this.valueType = valueType;
            this.unknownSimpleField = unknownSimpleField;
            this.attributePreferenceType = attributePreferenceType;
        }
    }
    public InformationTable convert(String ArffData){

        List<MinimalAttribute> minimalAttributes = new ObjectArrayList<MinimalAttribute>();
        String line;
        String[] tokens, elements = new String[0];
        String attributeName;
        String attributeDomain;
        ElementList elementList;
        EvaluationField valueType;
        InformationTable informationTable = null;


        String dataSeparator = "\\s*,\\s*";

        try (Scanner scanner = new Scanner(ArffData)){
            while (scanner.hasNext()){
                line = scanner.nextLine().trim();

                UnknownSimpleField unknownSimpleField = UnknownSimpleFieldMV2.getInstance();
                AttributePreferenceType attributePreferenceType = AttributePreferenceType.NONE;

                if (line.startsWith("@relation")) {
                    continue;
                }
                if (line.startsWith("@data")) {
                    break;
                }
                if (line.startsWith("@attribute")) {
                    tokens = line.split("\\s+");
                    if (tokens[1].contains("[")){
                        int nameEnd = tokens[1].indexOf("[");
                        attributeName = tokens[1].substring(0, nameEnd);
                    }
                    else{
                        attributeName = tokens[1];
                    }
                    //System.out.println(tokens[2]);
                    if (tokens[1].contains("[g]")){
                        attributePreferenceType = AttributePreferenceType.GAIN;
                    }
                    else if (tokens[1].contains("[c]")){
                        attributePreferenceType = AttributePreferenceType.COST;
                    }

                    if (tokens[1].contains("[mv1.5]")){
                        unknownSimpleField = UnknownSimpleFieldMV15.getInstance();
                    }
                    else if (tokens[1].contains("[mv2]")){
                        unknownSimpleField = UnknownSimpleFieldMV2.getInstance();
                    }

                    if (tokens[2].startsWith("{")){
                        int openingBraceIndex = line.indexOf("{");
                        int closingBraceIndex = line.indexOf("}");
                        attributeDomain = line.substring(openingBraceIndex + 1, closingBraceIndex).trim();
                        //System.out.println(attributeDomain);
                        elements = attributeDomain.split(dataSeparator);
                        //unquoteElements(elements);
                        //System.out.println(elements[0]);
                        try {
                            elementList = new ElementList(elements);
                        }catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        valueType = EnumerationFieldFactory.getInstance().create(elementList, EnumerationField.DEFAULT_VALUE, attributePreferenceType);
                        minimalAttributes.add(new MinimalAttribute(attributeName, valueType, unknownSimpleField, attributePreferenceType));
                    }
                    else{
                        if (tokens[2].trim().equals("numeric")){
                            valueType = RealFieldFactory.getInstance().create(RealField.DEFAULT_VALUE, attributePreferenceType);
                            minimalAttributes.add(new MinimalAttribute(attributeName, valueType, unknownSimpleField, attributePreferenceType));
                        }
                        //else if (tokens[2].trim().equals("numeric")){
                            //valueType = EvaluationFieldFactory()
                        //}

                    }
                }
            }

            Attribute[] rlAttributes = new EvaluationAttribute[minimalAttributes.size()];
            int attrIndex = 0;

            while (attrIndex < minimalAttributes.size() - 1){
                rlAttributes[attrIndex] = new EvaluationAttribute(
                        minimalAttributes.get(attrIndex).name,
                        true,
                        AttributeType.CONDITION,
                        minimalAttributes.get(attrIndex).valueType,
                        minimalAttributes.get(attrIndex).unknownSimpleField,
                        minimalAttributes.get(attrIndex).attributePreferenceType
                );
                attrIndex ++;
            }
            //System.out.println(attrIndex);

            rlAttributes[attrIndex] = new EvaluationAttribute(
                    minimalAttributes.get(attrIndex).name,
                    true,
                    AttributeType.DECISION,
                    minimalAttributes.get(attrIndex).valueType,
                    minimalAttributes.get(attrIndex).unknownSimpleField,
                    minimalAttributes.get(attrIndex).attributePreferenceType
                    );
            InformationTableBuilder informationTableBuilder = new InformationTableBuilder(rlAttributes, ",", new String[] {"?"});

            while(scanner.hasNext()){
                line = scanner.nextLine().trim();
                if (line.length() > 0){
                    elements = line.split(dataSeparator);
                    //unquoteElements(elements);
                    System.out.println(elements.length);
                }

                informationTableBuilder.addObject(elements);
            }
            informationTableBuilder.clearVolatileCaches();
            informationTable = informationTableBuilder.build();
        }
        return informationTable;
    }


    private void unquoteElements(String[] elements) {
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].startsWith("\"") && elements[i].endsWith("\"")) {
                elements[i] = elements[i].substring(1, elements[i].length() - 1); //remove double quotes
            } else {
                if (elements[i].startsWith("'") && elements[i].endsWith("'")) {
                    elements[i] = elements[i].substring(1, elements[i].length() - 1); //remove single quotes
                }
            }
        }
    }
}
