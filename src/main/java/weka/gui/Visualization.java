package weka.gui;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.rulelearn.data.Attribute;
import org.rulelearn.data.InformationTable;
import org.rulelearn.dominance.DominanceConeCalculator;
import org.rulelearn.rules.ApproximatedSetProvider;
import org.rulelearn.rules.RuleSetWithCharacteristics;
import org.rulelearn.types.EnumerationField;
import org.rulelearn.types.IntegerField;
import org.rulelearn.types.RealField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Visualization extends Frame {

    public static void run(
            ApproximatedSetProvider UnionsAtMost,
            ApproximatedSetProvider UnionsAtLeast,
            InformationTable informationTable,
            RuleSetWithCharacteristics ruleSet) {
        JFrame frame = new JFrame("Visualization");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 500);

        JTabbedPane tabbedPane = new JTabbedPane();
        /** Panels */
        JPanel ClassUnionsPanel = new JPanel();
        JPanel DominanceConesPanel = new JPanel();
        JPanel RulesPanel = new JPanel();

        /** Elements */
        JLabel UnionsHeaderLabel = new JLabel();
        JLabel DominanceConesLabel = new JLabel();
        JLabel RulesLabel = new JLabel();

        DefaultListModel<String> UnionsModel = new DefaultListModel<>();
        JList<String> UnionsList = new JList<>(UnionsModel);

        DefaultListModel<String> DominanceConesModel = new DefaultListModel<>();
        JList<String> DominanceConesList = new JList<>(DominanceConesModel);
        JScrollPane ScrollDominanceConesPane = new JScrollPane(DominanceConesList);

        DefaultListModel<String> RulesListModel = new DefaultListModel<>();
        JList<String> RulesList = new JList<>(RulesListModel);
        JScrollPane ScrollRulesPane = new JScrollPane(RulesList);

        tabbedPane.addTab("Dominance Cones", DominanceConesPanel);
        tabbedPane.addTab("Class Unions", ClassUnionsPanel);
        tabbedPane.addTab("Rules", RulesPanel);

        /*
          Dominance Cones Panel
         */
        DominanceConesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        DominanceConesPanel.setLayout(new BoxLayout(DominanceConesPanel, BoxLayout.Y_AXIS));
        DominanceConesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ScrollDominanceConesPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        DominanceConesPanel.add(DominanceConesLabel);
        DominanceConesPanel.add(ScrollDominanceConesPane);

        int NumberOfObjects = informationTable.getNumberOfObjects();
        IntSortedSet[] PositiveDCones = new IntSortedSet[NumberOfObjects];
        IntSortedSet[] NegativeDCones = new IntSortedSet[NumberOfObjects];
        IntSortedSet[] PositiveInvDCones = new IntSortedSet[NumberOfObjects];
        IntSortedSet[] NegativeInvDCones = new IntSortedSet[NumberOfObjects];
        
        for(int i = 0; i < NumberOfObjects; ++i) {
            PositiveDCones[i] = DominanceConeCalculator.INSTANCE.calculatePositiveDCone(i, informationTable);
            NegativeDCones[i] = DominanceConeCalculator.INSTANCE.calculateNegativeDCone(i, informationTable);
            PositiveInvDCones[i] = DominanceConeCalculator.INSTANCE.calculatePositiveInvDCone(i, informationTable);
            NegativeInvDCones[i] = DominanceConeCalculator.INSTANCE.calculateNegativeInvDCone(i, informationTable);
        }

        DominanceConesLabel.setText("NUMBER OF OBJECTS: " + informationTable.getNumberOfObjects());
        for (int i = 0; i < informationTable.getNumberOfObjects(); ++i) {
            DominanceConesModel.addElement("<html>Object " + (i + 1)
                    + "<br>Number of objects in positive dominance cone: "
                    + PositiveDCones[i].size()
                    + "<br>Number of objects in negative dominance cone: "
                    + NegativeDCones[i].size()
                    + "<br>Number of objects in positive inverse dominance cone: "
                    + PositiveInvDCones[i].size()
                    + "<br>Number of objects in negative inverse dominance cone: "
                    +NegativeInvDCones[i].size()
                    + "</html>");
        }
        MouseListener DominanceConeListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 1) {
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    if (index >= 0) {
                        System.out.println("Double-clicked on: " + index);
                        DominanceConesDialog(frame, informationTable, PositiveDCones[index], NegativeDCones[index], PositiveInvDCones[index], NegativeInvDCones[index], index);

                    }
                }
            }
        };
        DominanceConesList.addMouseListener(DominanceConeListener);

        /*
            Class Unions Panel
         */
        ClassUnionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        ClassUnionsPanel.setLayout(new BoxLayout(ClassUnionsPanel, BoxLayout.Y_AXIS));
        UnionsHeaderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UnionsList.setAlignmentX(Component.LEFT_ALIGNMENT);
        ClassUnionsPanel.add(UnionsHeaderLabel);
        ClassUnionsPanel.add(UnionsList);

        int UnionsCount = UnionsAtLeast.getCount() + UnionsAtMost.getCount();
        UnionsHeaderLabel.setText("NUMBER OF UNIONS: " + UnionsCount);

        for (int i = 0; i < UnionsAtMost.getCount(); ++i) {
            UnionsModel.addElement("<html><div><br>"
                    +  "<p>" + UnionsAtMost.getApproximatedSet(i) + "</p> <ul>"
                    + "<li>Accuracy of approximation: " + UnionsAtMost.getApproximatedSet(i).getAccuracyOfApproximation() + "</li>"
                    + "<li>Quality of approximation: " + UnionsAtMost.getApproximatedSet(i).getQualityOfApproximation() + "</li>"
                    + "</ul></div></html>"
            );
        }

        for (int i = 0; i < UnionsAtLeast.getCount(); ++i) {
            UnionsModel.addElement("<html><div><br>"
                    +  "<p>" + UnionsAtLeast.getApproximatedSet(i) + "</p> <ul>"
                    + "<li>Accuracy of approximation: " + UnionsAtLeast.getApproximatedSet(i).getAccuracyOfApproximation() + "</li>"
                    + "<li>Quality of approximation: " + UnionsAtLeast.getApproximatedSet(i).getQualityOfApproximation() + "</li>"
                    + "</ul></div></html>"
            );
        }

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 1) {
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    if (index >= 0) {
                        System.out.println("Double-clicked on: " + index);
                        if (index > 1){
                            index %= 2;
                            UnionDialog(frame, UnionsAtLeast, informationTable, index);
                        } else {
                            UnionDialog(frame, UnionsAtMost, informationTable, index);
                        }
                    }
                }
            }
        };
        UnionsList.addMouseListener(mouseListener);

        /*
          Rules Panel
        */
        RulesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        RulesPanel.setLayout(new BoxLayout(RulesPanel, BoxLayout.Y_AXIS));
        RulesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ScrollRulesPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        RulesPanel.add(RulesLabel);
        RulesPanel.add(ScrollRulesPane);

        RulesLabel.setText("NUMBER OF RULES: " + ruleSet.size());
        String RuleString;
        for (int i = 0; i < ruleSet.size(); ++i) {
            RuleString = "<html>" + ruleSet.getRule(i).getDecision();

            for (int j = 0; j < ruleSet.getRule(i).getConditions().length; ++j){
                RuleString += "<br>" + ruleSet.getRule(i).getConditions()[j];
            }
            RuleString += "<br>Support: " + ruleSet.getRuleCharacteristics(i).getSupport()
                    + " | Strength: " + ruleSet.getRuleCharacteristics(i).getStrength()
                    + " | Coverage Factor: " + ruleSet.getRuleCharacteristics(i).getCoverageFactor()
                    + " | Confidence: " + ruleSet.getRuleCharacteristics(i).getConfidence()
                    + " | Epsilon measure: " + ruleSet.getRuleCharacteristics(i).getEpsilon();

            RulesListModel.addElement(RuleString);
        }
        MouseListener RulesListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 1) {
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    if (index >= 0) {
                        System.out.println("Double-clicked on: " + index);
                        RulesDialog(frame, informationTable, ruleSet, index);
                    }
                }
            }
        };
        RulesList.addMouseListener(RulesListener);

        frame.getContentPane().add(tabbedPane);
        frame.setVisible(true);
    }

    public static void UnionDialog(
            JFrame frame,
            ApproximatedSetProvider union,
            InformationTable informationTable,
            int index){

        JPanel UnionPanel = new JPanel();
        JPanel ObjectsPanel = new JPanel();
        JPanel AttributesPanel = new JPanel();

        UnionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        ObjectsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        AttributesPanel.setBorder(new EmptyBorder(10, 10, 10,10));

        JLabel UnionCharacteristicLabel = new JLabel();
        JLabel ObjectsLabel = new JLabel();
        JLabel AttributesLabel = new JLabel();
        JLabel ValuesLabel = new JLabel();

        JTextArea UnionTextArea = new JTextArea();
        UnionTextArea.setText(union.getApproximatedSet(index)
                + "\nAccuracy of approximation " + union.getApproximatedSet(index).getAccuracyOfApproximation()
                +  "\nQuality of approximation " + union.getApproximatedSet(index).getQualityOfApproximation()
        );

        DefaultListModel<String> OptionsModel = new DefaultListModel<>();
        JList<String> OptionsList = new JList<>(OptionsModel);

        DefaultListModel<String> ObjectsModel = new DefaultListModel<>();
        JList<String> ObjectsList = new JList<>(ObjectsModel);
        JScrollPane ScrollObjectsPane = new JScrollPane(ObjectsList);

        DefaultListModel<String> AttributesModel = new DefaultListModel<>();
        JList<String> AttributesList = new JList<>(AttributesModel);
        JScrollPane ScrollAttributesPane = new JScrollPane(AttributesList);

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 1) {
                    int OptionIndex = theList.locationToIndex(mouseEvent.getPoint());
                    if (OptionIndex >= 0) {
                        System.out.println("Double-clicked on: " + OptionIndex);
                        ObjectsModel.clear();
                        ObjectsLabel.setVisible(true);
                        ObjectsList.setVisible(true);
                        int[] objects = new int[]{0};
                        switch (OptionIndex){
                            case 0:
                                ObjectsLabel.setText("OBJECTS");
                                objects = union.getApproximatedSet(index).getObjects().toIntArray();
                                break;
                            case 1:
                                ObjectsLabel.setText("LOWER APPROXIMATION");
                                objects = union.getApproximatedSet(index).getLowerApproximation().toIntArray();
                                break;
                            case 2:
                                ObjectsLabel.setText("UPPER APPROXIMATION");
                                objects = union.getApproximatedSet(index).getUpperApproximation().toIntArray();
                                break;
                            case 3:
                                ObjectsLabel.setText("BOUNDARY");
                                objects = union.getApproximatedSet(index).getBoundary().toIntArray();
                                break;
                            case 4:
                                ObjectsLabel.setText("POSITIVE REGION");
                                objects = union.getApproximatedSet(index).getPositiveRegion().toIntArray();
                                break;
                            case 5:
                                ObjectsLabel.setText("NEGATIVE REGION");
                                objects = union.getApproximatedSet(index).getNegativeRegion().toIntArray();
                                break;
                            case 6:
                                ObjectsLabel.setText("BOUNDARY REGION");
                                objects = union.getApproximatedSet(index).getBoundaryRegion().toIntArray();
                                break;
                            default:
                                break;
                        }
                        for (int i = 0; i < objects.length; ++i){
                            ObjectsModel.addElement("Object " + String.valueOf(objects[i] + 1));
                        }

                    }
                }
            }
        };

        MouseListener attributesListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 1){
                    AttributesList.setVisible(true);
                    AttributesLabel.setVisible(true);
                    int ObjIndex = theList.locationToIndex(mouseEvent.getPoint());
                    if (ObjIndex >= 0) {
                        System.out.println("Clicked on: " + ObjIndex);
                        System.out.println(ObjectsModel.get(ObjIndex));
                        AttributesModel.clear();
                        for (int i = 0; i < informationTable.getNumberOfAttributes(); ++i){
                            AttributesModel.addElement(informationTable.getAttribute(i).getName() + " "
                                    + informationTable.getField(Integer.valueOf(ObjectsModel.get(ObjIndex).split(" ")[1]) - 1, i));
                        }
                    }
                }
            }
        };
        OptionsModel.addElement("Objects " + union.getApproximatedSet(index).getObjects().size());
        OptionsModel.addElement("Lower Approximation " + union.getApproximatedSet(index).getLowerApproximation().size());
        OptionsModel.addElement("Upper Approximation " + union.getApproximatedSet(index).getUpperApproximation().size());
        OptionsModel.addElement("Boundary " + union.getApproximatedSet(index).getBoundary().size());
        OptionsModel.addElement("Positive region " + union.getApproximatedSet(index).getPositiveRegion().size());
        OptionsModel.addElement("Negative region " + union.getApproximatedSet(index).getNegativeRegion().size());
        OptionsModel.addElement("Boundary region " + union.getApproximatedSet(index).getBoundaryRegion().size());
        OptionsList.addMouseListener(mouseListener);
        OptionsList.setAlignmentX(Component.LEFT_ALIGNMENT);
        UnionCharacteristicLabel.setText("UNION'S CHARACTERISTICS");
        UnionCharacteristicLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UnionPanel.setLayout(new BoxLayout(UnionPanel, BoxLayout.Y_AXIS));
        UnionPanel.add(UnionCharacteristicLabel);
        UnionPanel.add(OptionsList);

        ObjectsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ScrollObjectsPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        ObjectsList.setVisible(false);
        ObjectsLabel.setVisible(false);
        AttributesLabel.setVisible(false);
        AttributesList.setVisible(false);

        ObjectsPanel.setLayout(new BoxLayout(ObjectsPanel, BoxLayout.Y_AXIS));
        ObjectsPanel.add(ObjectsLabel);
        ObjectsPanel.add(ScrollObjectsPane);
        ObjectsList.addMouseListener(attributesListener);

        AttributesPanel.setLayout(new BoxLayout(AttributesPanel, BoxLayout.Y_AXIS));
        AttributesLabel.setText("Attribute Name");
        ValuesLabel.setText("Name        Value");
        ValuesLabel.setHorizontalAlignment(SwingConstants.LEFT);

        ValuesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UnionTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        AttributesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ScrollAttributesPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        AttributesPanel.add(ValuesLabel);
        AttributesPanel.add(UnionTextArea);
        AttributesPanel.add(AttributesLabel);
        AttributesPanel.add(ScrollAttributesPane);

        JDialog dialog = new JDialog(frame, "Union Details", true);
        dialog.setSize(750,450);
        dialog.setLocationRelativeTo(frame);
        dialog.add(UnionPanel, BorderLayout.WEST);
        dialog.add(ObjectsPanel, BorderLayout.CENTER);
        dialog.add(AttributesPanel, BorderLayout.EAST);
        dialog.setVisible(true);
    }

    public static void DominanceConesDialog(
            JFrame frame,
            InformationTable informationTable,
            IntSortedSet positiveDCone,
            IntSortedSet negativeDCone,
            IntSortedSet positiveInvDCone,
            IntSortedSet negativeInvDCone,
            int index){

        JPanel DominanceConesPanel = new JPanel();
        JPanel DomimanceConeObjectsPanel = new JPanel();
        JPanel RelationsPanel = new JPanel();

        DominanceConesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        DomimanceConeObjectsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        RelationsPanel.setBorder(new EmptyBorder(10, 10, 10,10));

        DominanceConesPanel.setLayout(new BoxLayout(DominanceConesPanel, BoxLayout.Y_AXIS));
        DomimanceConeObjectsPanel.setLayout(new BoxLayout(DomimanceConeObjectsPanel, BoxLayout.Y_AXIS));
        RelationsPanel.setLayout(new BoxLayout(RelationsPanel, BoxLayout.Y_AXIS));

        JLabel DominanceConesLabel = new JLabel("DOMINANCE CONES");
        JLabel DominanceConeNameLabel = new JLabel();
        JLabel ObjectsLabel = new JLabel();

        DefaultListModel<String> DominanceConesModel = new DefaultListModel<>();
        JList<String> DominanceConesList = new JList<>(DominanceConesModel);

        DefaultListModel<String> ObjectsModel = new DefaultListModel<>();
        JList<String> ObjectsList = new JList<>(ObjectsModel);

        JScrollPane ScrollObjectsPane = new JScrollPane(ObjectsList);


        DefaultTableModel RelationsTableModel = new DefaultTableModel();
        JTable RelationsTable = new JTable(RelationsTableModel);
        JTableHeader RelationsTableHeader = RelationsTable.getTableHeader();

        RelationsTableModel.addColumn("ATTRIBUTE NAME");
        RelationsTableModel.addColumn("OBJECT1");
        RelationsTableModel.addColumn("RELATION");
        RelationsTableModel.addColumn("OBJECT2");


        DominanceConesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        DominanceConesList.setAlignmentX(Component.LEFT_ALIGNMENT);
        DominanceConesModel.addElement("Positive dominance cone " + positiveDCone.size());
        DominanceConesModel.addElement("Negative dominance cone " + negativeDCone.size());
        DominanceConesModel.addElement("Positive inverse dominance cone " + positiveInvDCone.size());
        DominanceConesModel.addElement("Negative inverse dominance cone " + negativeInvDCone.size());
        DominanceConesPanel.add(DominanceConesLabel);
        DominanceConesPanel.add(DominanceConesList);

        DominanceConeNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ScrollObjectsPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        DomimanceConeObjectsPanel.add(DominanceConeNameLabel);
        DomimanceConeObjectsPanel.add(ScrollObjectsPane);
        ScrollObjectsPane.setVisible(false);
        ObjectsLabel.setVisible(false);

        ObjectsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        RelationsTable.setAlignmentX(Component.LEFT_ALIGNMENT);
        RelationsPanel.add(ObjectsLabel);
        RelationsPanel.add(RelationsTable);
        MouseListener DominanceConesListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 1){
                    ScrollObjectsPane.setVisible(true);
                    DominanceConeNameLabel.setVisible(true);
                    int Coneindex = theList.locationToIndex(mouseEvent.getPoint());
                    if (Coneindex >= 0) {
                        System.out.println("Clicked on: " + Coneindex);
                        ObjectsModel.clear();

                        switch (Coneindex) {
                            case 0:
                                DominanceConeNameLabel.setText("POSITIVE DOMINANCE CONE");
                                for (int i = 0; i < positiveDCone.size(); i++) {
                                    ObjectsModel.addElement("Object " + (Integer.valueOf(positiveDCone.toArray()[i].toString()) + 1));
                                }
                                break;
                            case 1:
                                DominanceConeNameLabel.setText("NEGATIVE DOMINANCE CONE");
                                for (int i = 0; i < negativeDCone.size(); i++) {
                                    ObjectsModel.addElement("Object " + (Integer.valueOf(negativeDCone.toArray()[i].toString()) + 1));
                                }
                                break;
                            case 2:
                                DominanceConeNameLabel.setText("POSITIVE INVERSE DOMINANCE CONE");
                                for (int i = 0; i < positiveInvDCone.size(); i++) {
                                    ObjectsModel.addElement("Object " + (Integer.valueOf(positiveInvDCone.toArray()[i].toString()) + 1));
                                }
                                break;
                            case 3:
                                DominanceConeNameLabel.setText("NEGATIVE INVERSE DOMINANCE CONE");
                                for (int i = 0; i < negativeInvDCone.size(); i++) {
                                    ObjectsModel.addElement("Object " + (Integer.valueOf(negativeInvDCone.toArray()[i].toString()) + 1));
                                }
                                break;
                            default:
                                break;

                        }
                    }
                }
            }
        };
        DominanceConesList.addMouseListener(DominanceConesListener);
        MouseListener ObjectListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 1){

                    //DominanceConeNameLabel.setVisible(true);
                    RelationsTableModel.getDataVector().removeAllElements();
                    int ObjIndex = theList.locationToIndex(mouseEvent.getPoint());
                    ObjIndex = Integer.valueOf(ObjectsModel.get(ObjIndex).split(" ")[1]) - 1;
                    if (ObjIndex >= 0) {
                        String relation = null;
                        System.out.println("Clicked on: " + ObjIndex);
                        ObjectsLabel.setText("ATTR NAME OBJECT  " + (index + 1) + "  RELATION " + " OBJECT " + (ObjIndex + 1));
                        ObjectsLabel.setVisible(true);
                        RelationsTable.setTableHeader(RelationsTableHeader);
                        for (int i = 0; i < informationTable.getNumberOfAttributes(); ++i){
                            relation = getRelation(informationTable, index, ObjIndex, i);
                            RelationsTableModel.addRow(new Object[]{
                                    informationTable.getAttribute(i).getName(),
                                    informationTable.getField(index, i), relation, informationTable.getField(ObjIndex, i)});
                        }

                    }
                }
            }
        };
        ObjectsList.addMouseListener(ObjectListener);


        JDialog dialog = new JDialog(frame, "Dominance Cones", true);
        dialog.setSize(750,450);
        dialog.setLocationRelativeTo(frame);
        dialog.add(DominanceConesPanel, BorderLayout.WEST);
        dialog.add(DomimanceConeObjectsPanel, BorderLayout.CENTER);
        dialog.add(RelationsPanel, BorderLayout.EAST);
        dialog.setVisible(true);
    }

    public static void RulesDialog(
            JFrame frame,
            InformationTable informationTable,
            RuleSetWithCharacteristics ruleSet,
            int index){

        JPanel CharacteristicPanel = new JPanel();
        JPanel CoveredObjectsPanel = new JPanel();
        JPanel ObjectPanel = new JPanel();

        CharacteristicPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        CoveredObjectsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        ObjectPanel.setBorder(new EmptyBorder(10, 10, 10,10));

        CharacteristicPanel.setLayout(new BoxLayout(CharacteristicPanel, BoxLayout.Y_AXIS));
        CoveredObjectsPanel.setLayout(new BoxLayout(CoveredObjectsPanel, BoxLayout.Y_AXIS));
        ObjectPanel.setLayout(new BoxLayout(ObjectPanel, BoxLayout.Y_AXIS));

        JLabel CharacteristicLabel = new JLabel("CHARACTERISTIC         VALUE");
        JLabel CoveredObjectsLabel = new JLabel("COVERED OBJECTS");
        JLabel ObjectNameLabel = new JLabel("ATTRIBUTE NAME");


        DefaultListModel<String> CharacteristicListModel = new DefaultListModel<>();
        JList<String> CharacteristicList = new JList<>(CharacteristicListModel);

        DefaultListModel<String> ObjectsModel = new DefaultListModel<>();
        JList<String> ObjectsList = new JList<>(ObjectsModel);
        JScrollPane ScrollObjectsPane = new JScrollPane(ObjectsList);

        DefaultListModel<String> AttributesModel = new DefaultListModel<>();
        JList<String> AttributesList = new JList<>(AttributesModel);
        JScrollPane ScrollAttributesPane = new JScrollPane(AttributesList);


        CharacteristicLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        CharacteristicList.setAlignmentX(Component.LEFT_ALIGNMENT);

        CharacteristicListModel.addElement(String.format("%-40s", "Support") + ruleSet.getRuleCharacteristics(index).getSupport());
        CharacteristicListModel.addElement(String.format("%-40s", "Strength") + ruleSet.getRuleCharacteristics(index).getStrength());
        CharacteristicListModel.addElement(String.format("%-40s", "Confidence") + ruleSet.getRuleCharacteristics(index).getConfidence());
        CharacteristicListModel.addElement(String.format("%-40s", "Coverage factor") + ruleSet.getRuleCharacteristics(index).getCoverageFactor());
        CharacteristicListModel.addElement(String.format("%-40s", "Coverage") + ruleSet.getRuleCharacteristics(index).getCoverage());
        CharacteristicListModel.addElement(String.format("%-40s", "Negative coverage") + ruleSet.getRuleCharacteristics(index).getNegativeCoverage());
        CharacteristicListModel.addElement(String.format("%-40s", "Epsilon measure") + ruleSet.getRuleCharacteristics(index).getEpsilon());
        CharacteristicListModel.addElement(String.format("%-40s", "Epsilon prime measure") + ruleSet.getRuleCharacteristics(index).getEpsilonPrime());
        CharacteristicListModel.addElement(String.format("%-40s", "F-confirmation measure") + ruleSet.getRuleCharacteristics(index).getFConfirmation());
        CharacteristicListModel.addElement(String.format("%-40s", "A-confirmation measure") + ruleSet.getRuleCharacteristics(index).getAConfirmation());
        CharacteristicListModel.addElement(String.format("%-40s", "Z-confirmation measure") + ruleSet.getRuleCharacteristics(index).getZConfirmation());
        CharacteristicListModel.addElement(String.format("%-40s", "L-confirmation measure") + ruleSet.getRuleCharacteristics(index).getLConfirmation());
        CharacteristicListModel.addElement(String.format("%-40s", "c1-confirmation measure") + ruleSet.getRuleCharacteristics(index).getC1Confirmation());
        CharacteristicListModel.addElement(String.format("%-40s", "S-confirmation measure") + ruleSet.getRuleCharacteristics(index).getSConfirmation());
        CharacteristicListModel.addElement(String.format("%-40s", "Length") + ruleSet.getRule(index).getConditions().length);

        CharacteristicPanel.add(CharacteristicLabel);
        CharacteristicPanel.add(CharacteristicList);

        for (int i = 0; i < informationTable.getNumberOfObjects(); ++i){
            if( ruleSet.getRule(index).covers(i, informationTable)){
                ObjectsModel.addElement("Object " + (i + 1));
            }

        }
        CoveredObjectsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ScrollObjectsPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        CoveredObjectsPanel.add(CoveredObjectsLabel);
        CoveredObjectsPanel.add(ScrollObjectsPane);

        MouseListener ObjectsListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 1){
                    int ObjIndex = theList.locationToIndex(mouseEvent.getPoint());
                    if (ObjIndex >= 0) {
                        System.out.println("Clicked on: " + ObjIndex);
                        System.out.println(ObjectsModel.get(ObjIndex));
                        AttributesModel.clear();
                        for (int i = 0; i < informationTable.getNumberOfAttributes(); ++i){
                            AttributesModel.addElement(informationTable.getAttribute(i).getName() + " "
                                    + informationTable.getField(Integer.valueOf(ObjectsModel.get(ObjIndex).split(" ")[1]) - 1, i));
                        }
                    }
                }
            }
        };
        ObjectsList.addMouseListener(ObjectsListener);

        ObjectNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ScrollAttributesPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        ObjectPanel.add(ObjectNameLabel);
        ObjectPanel.add(ScrollAttributesPane);

        JDialog dialog = new JDialog(frame, "Dominance Cones", true);
        dialog.setSize(750,450);
        dialog.setLocationRelativeTo(frame);
        dialog.add(CharacteristicPanel, BorderLayout.WEST);
        dialog.add(CoveredObjectsPanel, BorderLayout.CENTER);
        dialog.add(ObjectPanel, BorderLayout.EAST);
        dialog.setVisible(true);
    }

    public static String getRelation(InformationTable informationTable, int firstObject, int secondObject, int index) {
        String relation = null;

        if (informationTable.getField(firstObject, index).equals(informationTable.getField(secondObject, index))){
            relation= "=";
        }
        else if (informationTable.getField(firstObject, index).toString().contains("?") || informationTable.getField(secondObject, index).toString().contains("?")) {
            relation = "=";
        }
        else{
            if (informationTable.getAttribute(index).getValueType() instanceof IntegerField) {
                if (informationTable.getField(firstObject, index).getTypeDescriptor().toString().contains("gain")){
                    relation = Integer.parseInt(informationTable.getField(firstObject, index).toString())
                            > Integer.parseInt(informationTable.getField(secondObject, index).toString()) ? ">" : "<";
                }
                else {
                    relation = Integer.parseInt(informationTable.getField(firstObject, index).toString())
                            < Integer.parseInt(informationTable.getField(secondObject, index).toString()) ? ">" : "<";
                }
            } else if (informationTable.getAttribute(index).getValueType() instanceof RealField) {
                if (informationTable.getField(firstObject, index).getTypeDescriptor().toString().contains("gain")){
                    relation = Float.parseFloat(informationTable.getField(firstObject, index).toString())
                            > Float.parseFloat(informationTable.getField(secondObject, index).toString()) ? ">" : "<";
                }
                else {
                    relation = Float.parseFloat(informationTable.getField(firstObject, index).toString())
                            < Float.parseFloat(informationTable.getField(secondObject, index).toString()) ? ">" : "<";
                }
            }
            else if (informationTable.getAttribute(index).getValueType() instanceof EnumerationField){

                System.out.println(informationTable.getField(firstObject, index).getTypeDescriptor().toString());

                String AttributeEnumsString = informationTable.getAttribute(index).serialize().toString();

                String[] AttributeEnums = AttributeEnumsString.substring(AttributeEnumsString.indexOf("(") + 1, AttributeEnumsString.indexOf(")")).split(",");
                int first_index = 0, second_index = 0;
                for (int i = 0; i < AttributeEnums.length; ++i){
                    if (AttributeEnums[i].equals(informationTable.getField(firstObject, index).toString())) {
                        first_index = i;
                    }
                    if (AttributeEnums[i].equals(informationTable.getField(secondObject, index).toString())) {
                        second_index = i;
                    }
                    if (informationTable.getField(firstObject, index).getTypeDescriptor().toString().contains("gain")){
                        relation = first_index > second_index ? "<" : ">";
                    }
                    else if (informationTable.getField(firstObject, index).getTypeDescriptor().toString().contains("cost")){
                        relation = first_index > second_index ? ">" : "<";
                    }

                }
            }

        }

        return relation;
    }

}
