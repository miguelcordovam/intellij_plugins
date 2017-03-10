package com.restdocs.toolwindow;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.restdocs.action.common.HttpMethod;
import com.restdocs.action.common.RestServiceNode;
import com.restdocs.action.common.RestTreeCellRenderer;
import com.restdocs.action.util.CopyRestUrlUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ShowRestServicesForm {

    private JPanel contentPanel = new JPanel();
    private JTree servicesTree;
    private JScrollPane scrollPane;
    private JLabel status;

    private CopyRestUrlUtil copyRestUrlUtil;

    public ShowRestServicesForm(Project project) {
        scrollPane = new JBScrollPane(servicesTree, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        status.setBackground(Color.gray);

        copyRestUrlUtil = new CopyRestUrlUtil(project);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.setBorder(BorderFactory.createEmptyBorder());

        scrollPane.setBackground(Color.white);
        contentPanel.setLayout(new GridLayoutManager(2, 1));
        contentPanel.add(scrollPane,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));

        contentPanel.add(status, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        contentPanel.setVisible(true);
        servicesTree.setModel(null);
        servicesTree.setCellRenderer(new RestTreeCellRenderer());

        JPopupMenu popupMenu = createPopupMenu();

        servicesTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = servicesTree.getRowForLocation(e.getX(), e.getY());
                    if (row != -1) {
                        servicesTree.setSelectionRow(row);
                        DefaultMutableTreeNode selectedNode =
                                (DefaultMutableTreeNode) servicesTree.getLastSelectedPathComponent();

                        if (selectedNode.getUserObject() instanceof RestServiceNode) {
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }
        });
    }

    @NotNull
    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyRestUrlAction = new JMenuItem("Copy REST Url");
        JMenuItem copyCurlAction = new JMenuItem("Copy cURL");
        popupMenu.add(copyRestUrlAction);
        popupMenu.add(copyCurlAction);

        copyRestUrlAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode selectedNode =
                        (DefaultMutableTreeNode) servicesTree.getLastSelectedPathComponent();

                if (selectedNode.getUserObject() instanceof RestServiceNode) {
                    RestServiceNode serviceNode = (RestServiceNode) selectedNode.getUserObject();
                    String partialUrl = ((RestServiceNode) selectedNode.getUserObject()).getUrl();

                    CopyPasteManager.getInstance().setContents(new StringSelection(copyRestUrlUtil.getFullUrl(partialUrl,
                            serviceNode)));
                }
            }
        });

        copyCurlAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode selectedNode =
                        (DefaultMutableTreeNode) servicesTree.getLastSelectedPathComponent();

                if (selectedNode.getUserObject() instanceof RestServiceNode) {
                    RestServiceNode serviceNode = (RestServiceNode) selectedNode.getUserObject();
                    String partialUrl = serviceNode.getUrl();

                    String fullUrl = copyRestUrlUtil.getFullUrl(partialUrl, serviceNode);

                    StringBuilder curl = new StringBuilder("curl -X ");
                    curl.append(serviceNode.getMethod());
                    curl.append(" " + fullUrl);

                    CopyPasteManager.getInstance().setContents(
                            new StringSelection(curl.toString()));
                }
            }
        });

        return popupMenu;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public JTree getServicesTree() {
        return servicesTree;
    }

    public void setStatusText(String status) {
        this.status.setText(status);
    }
}