package com.restdocs.toolwindow;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.restdocs.action.common.RestTreeCellRenderer;

import javax.swing.*;
import java.awt.*;

public class ShowRestServicesForm {

    private JPanel contentPanel = new JPanel();
    private JTree servicesTree;
    private JScrollPane scrollPane;

    public ShowRestServicesForm() {
        scrollPane = new JBScrollPane(servicesTree, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.setBorder(BorderFactory.createEmptyBorder());

        contentPanel.setBackground(Color.white);
        contentPanel.setLayout(new GridLayoutManager(1, 1));
        contentPanel.add(scrollPane,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));
        contentPanel.setVisible(true);
        servicesTree.setModel(null);
        servicesTree.setCellRenderer(new RestTreeCellRenderer());
    }


    public JPanel getContentPanel() {
        return contentPanel;
    }

    public JTree getServicesTree() {
        return servicesTree;
    }
}