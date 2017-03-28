package com.restdocs.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.util.OpenSourceUtil;
import com.restdocs.action.common.HttpMethod;
import com.restdocs.action.common.RestServiceNode;
import com.restdocs.action.util.ServicesUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class RestServicesController {

    private RestServicesUI ui;
    private Map<String, List<RestServiceNode>> allServices;
    private ServicesUtil servicesUtil = new ServicesUtil();

    public void init(Project project) {
        ui = new RestServicesUI(project);
        allServices = loadServices(project);
        showServicesInTree(allServices);
        addListeners();
    }

    private Map<String, List<RestServiceNode>> loadServices(Project project) {
        return servicesUtil.getAllServicesByModule(project);
    }

    private void showServicesInTree(Map<String, List<RestServiceNode>> services) {
        int totalServices = 0;

        ui.getServicesTree().setModel(null);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("REST Services");

        for (String moduleName : services.keySet()) {
            List<RestServiceNode> servicesByModule = services.get(moduleName);
            DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(moduleName);

            Map<HttpMethod, List<RestServiceNode>> groupedByHttpMethod = servicesByModule.stream()
                    .collect(groupingBy(RestServiceNode::getMethod));

            for (HttpMethod method : groupedByHttpMethod.keySet()) {
                List<RestServiceNode> servicesByMethod = groupedByHttpMethod.get(method);
                DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode(method);

                for (RestServiceNode service : servicesByMethod) {
                    DefaultMutableTreeNode restService = new DefaultMutableTreeNode(service);
                    methodNode.add(restService);
                    totalServices++;
                }

                moduleNode.add(methodNode);
            }
            root.add(moduleNode);
        }

        ui.setStatusText("  " + totalServices  + (totalServices == 1 ? " service" : " services"));
        addSelectionTreeListener(root);
    }

    private void addListeners() {
        JTextField query = ui.getQuery();

        ActionListener searchListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String queryText = query.getText();
                Map<String, List<RestServiceNode>> filteredServices = new HashMap<>();

                for (String moduleName : allServices.keySet()) {
                    List<RestServiceNode> services = allServices.get(moduleName)
                            .stream()
                            .filter(r -> servicesUtil.matches(r.getUrl(), queryText))
                            .collect(Collectors.toList());

                    if (services.size() > 0) {
                        filteredServices.put(moduleName, services);
                    }
                }

                showServicesInTree(filteredServices);
                expandAll(ui.getServicesTree());
            }
        };

        query.addActionListener(searchListener);
        ui.getSearch().addActionListener(searchListener);
        ui.getClear().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                query.setText("");
                showServicesInTree(allServices);
            }
        });
    }

    private void addSelectionTreeListener(DefaultMutableTreeNode root) {
        DefaultTreeModel model = new DefaultTreeModel(root);
        ui.getServicesTree().setModel(model);

        ui.getServicesTree().getSelectionModel().setSelectionMode(SINGLE_TREE_SELECTION);
        ui.getServicesTree().addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodeSelected = (DefaultMutableTreeNode) ui.getServicesTree().getLastSelectedPathComponent();

            if (nodeSelected == null) return;

            if (nodeSelected.getUserObject() instanceof RestServiceNode) {
                RestServiceNode service = (RestServiceNode) nodeSelected.getUserObject();
                navigateToCode(service);
            }
        });
    }

    private void navigateToCode(RestServiceNode service) {
        OpenSourceUtil.navigate(true, service.getPsiMethod());
    }

    public RestServicesUI getUi() {
        return ui;
    }

    private void expandAll(JTree tree) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root));
    }

    private void expandAll(JTree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path);
            }
        }
        tree.expandPath(parent);
    }
}
