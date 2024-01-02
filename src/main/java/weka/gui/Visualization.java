package weka.gui;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.rulelearn.data.InformationTable;
import org.rulelearn.dominance.DominanceConeCalculator;
import org.rulelearn.rules.ApproximatedSetProvider;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Visualization extends Frame {

    public static void run(String ruleSet, ApproximatedSetProvider UnionsAtMost, ApproximatedSetProvider UnionsAtLeast, InformationTable informationTable) {
        JFrame frame = new JFrame("Visualization");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 500);

        JTabbedPane tabbedPane = new JTabbedPane();
        /** Panels */
        JPanel ClassUnionsPanel = new JPanel();
        JPanel DominanceConesPanel = new JPanel();
        JPanel RulesPanel = new JPanel();

        /** Elements */
        JTextArea RulesTextArea = new JTextArea();
        JLabel UnionsHeaderLabel = new JLabel();
        JLabel DominanceConesLabel = new JLabel();

        DefaultListModel<String> UnionsModel = new DefaultListModel<>();
        JList<String> UnionsList = new JList<>(UnionsModel);

        DefaultListModel<String> DominanceConesModel = new DefaultListModel<>();
        JList<String> DominanceConesList = new JList<>(DominanceConesModel);
        JScrollPane ScrollDominanceConesPane = new JScrollPane(DominanceConesList);

        tabbedPane.addTab("Dominance Cones", DominanceConesPanel);
        tabbedPane.addTab("Class Unions", ClassUnionsPanel);
        tabbedPane.addTab("Rules", RulesPanel);

        DominanceConesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        DominanceConesPanel.setLayout(new BoxLayout(DominanceConesPanel, BoxLayout.Y_AXIS));
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

        RulesPanel.add(RulesTextArea);
        RulesTextArea.append(ruleSet);

        ClassUnionsPanel.setLayout(new BoxLayout(ClassUnionsPanel, BoxLayout.Y_AXIS));
        ClassUnionsPanel.add(UnionsHeaderLabel);
        ClassUnionsPanel.add(UnionsList);

        int UnionsCount = UnionsAtLeast.getCount() + UnionsAtMost.getCount();
        UnionsHeaderLabel.setText("NUMBER OF UNIONS: " + UnionsCount);

        for (int i = 0; i < UnionsAtMost.getCount(); ++i) {
            UnionsModel.addElement("<html><div style='border: 1px solid black; padding: 10px;'><br>"
                    +  "<p>" + UnionsAtMost.getApproximatedSet(i) + "</p> <ul>"
                    + "<li>Accuracy of approximation: " + UnionsAtMost.getApproximatedSet(i).getAccuracyOfApproximation() + "</li>"
                    + "<li>Quality of approximation: " + UnionsAtMost.getApproximatedSet(i).getQualityOfApproximation() + "</li>"
                    + "</ul></div></html>"
            );
        }

        for (int i = 0; i < UnionsAtLeast.getCount(); ++i) {
            UnionsModel.addElement("<html><div style='border: 1px solid black; padding: 10px;'><br>"
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

        frame.getContentPane().add(tabbedPane);
        frame.setVisible(true);
    }

    public static void UnionDialog(JFrame frame, ApproximatedSetProvider union, InformationTable informationTable, int index){

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

        DefaultListModel<String> AttributesModel = new DefaultListModel<>();
        JList<String> AttributesList = new JList<>(AttributesModel);

        JScrollPane ScrollObjectsPane = new JScrollPane(ObjectsList);
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


        DefaultListModel<String> DominanceConesModel = new DefaultListModel<>();
        JList<String> DominanceConesList = new JList<>(DominanceConesModel);

        DefaultListModel<String> ObjectsModel = new DefaultListModel<>();
        JList<String> ObjectsList = new JList<>(ObjectsModel);

        JScrollPane ScrollObjectsPane = new JScrollPane(ObjectsList);


        DefaultTableModel RelationsTableModel = new DefaultTableModel();
        JTable RelationsTable = new JTable(RelationsTableModel);
        RelationsTableModel.addColumn("ATTRIBUTE NAME");
        RelationsTableModel.addColumn("OBJECT1");
        RelationsTableModel.addColumn("RELATION");
        RelationsTableModel.addColumn("OBJECT2");
        RelationsTable.setAlignmentX(Component.LEFT_ALIGNMENT);

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
                        for (int i = 0; i < informationTable.getNumberOfAttributes(); ++i){
                            if (informationTable.getField(index, i).isDifferentThan(informationTable.getField(ObjIndex, i)).toString() == "FALSE"){
                                relation= "=";
                            }
                            else{
                                relation = "!=";
                            }


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
}
