package weka.gui;

import org.rulelearn.data.InformationTable;
import org.rulelearn.rules.ApproximatedSetProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class Visualization extends Frame {

    public static void run(String ruleSet, ApproximatedSetProvider UnionsAtMost, ApproximatedSetProvider UnionsAtLeast, InformationTable informationTable) {
        JFrame frame = new JFrame("Visualization");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(750, 450);

        JTabbedPane tabbedPane = new JTabbedPane();
        /** Panels */
        JPanel ClassUnions = new JPanel();
        JPanel DominanceCones = new JPanel();
        JPanel Rules = new JPanel();

        /** Elements */
        JTextArea RulesSting = new JTextArea();
        JLabel UnionsHeaderLabel = new JLabel();
        DefaultListModel<String> UnionsModel = new DefaultListModel<>();
        JList<String> UnionsList = new JList<>(UnionsModel);

        tabbedPane.addTab("Dominance Cones", DominanceCones);
        tabbedPane.addTab("Class Unions", ClassUnions);
        tabbedPane.addTab("Rules", Rules);

        Rules.add(RulesSting);
        RulesSting.append(ruleSet);
        ClassUnions.setLayout(new BoxLayout(ClassUnions, BoxLayout.Y_AXIS));
        ClassUnions.add(UnionsHeaderLabel);
        ClassUnions.add(UnionsList);

        int UnionsCount = UnionsAtLeast.getCount() + UnionsAtMost.getCount();
        UnionsHeaderLabel.setText("NUMBER OF UNIONS: " + UnionsCount);

        for (int i = 0; i < UnionsAtMost.getCount(); ++i) {
            UnionsModel.addElement("<html><br>"
                    +  "<p>" + UnionsAtMost.getApproximatedSet(i) + "</p> <ul>"
                    + "<li>Accuracy of approximation: " + UnionsAtMost.getApproximatedSet(i).getAccuracyOfApproximation() + "</li>"
                    + "<li>Quality of approximation: " + UnionsAtMost.getApproximatedSet(i).getQualityOfApproximation() + "</li>"
                    + "</ul></html>"
            );
        }

        for (int i = 0; i < UnionsAtLeast.getCount(); ++i) {
            UnionsModel.addElement("<html><br>"
                    +  "<p>" + UnionsAtLeast.getApproximatedSet(i) + "</p> <ul>"
                    + "<li>Accuracy of approximation: " + UnionsAtLeast.getApproximatedSet(i).getAccuracyOfApproximation() + "</li>"
                    + "<li>Quality of approximation: " + UnionsAtLeast.getApproximatedSet(i).getQualityOfApproximation() + "</li>"
                    + "</ul></html>"
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
                            ObjectsModel.addElement(String.valueOf(objects[i] + 1));
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
                                    + informationTable.getField(Integer.valueOf(ObjectsModel.get(ObjIndex)) - 1, i));
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
        UnionCharacteristicLabel.setText("UNION'S CHARACTERISTICS");
        UnionCharacteristicLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        UnionPanel.setLayout(new BoxLayout(UnionPanel, BoxLayout.Y_AXIS));
        UnionPanel.add(UnionCharacteristicLabel, BorderLayout.EAST);
        UnionPanel.add(OptionsList);

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
        ValuesLabel.setText("Name     Value");
        ValuesLabel.setHorizontalAlignment(SwingConstants.LEFT);
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
}
