package com.restdocs.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.OpenSourceUtil;
import com.restdocs.action.common.HttpMethod;
import com.restdocs.action.common.RestServiceNode;
import com.restdocs.action.util.Util;
import com.restdocs.toolwindow.ShowRestServicesForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT;
import static java.util.stream.Collectors.groupingBy;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class ShowRestServicesAction extends AnAction {

    public static final String TOOL_WINDOW_ID = "REST Services";
    private Util util = new Util();
    private ShowRestServicesForm ui = new ShowRestServicesForm();

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        loadServicesOnBackground(project);

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow != null && toolWindow.isVisible()) {
            toolWindow.hide(null);
            ToolWindowManager.getInstance(project).unregisterToolWindow(TOOL_WINDOW_ID);
        }
    }

    private void loadServicesOnBackground(Project project) {
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                ApplicationManager.getApplication().runReadAction(() ->
                        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading Rest Services") {
                            @Override
                            public void run(@NotNull ProgressIndicator indicator) {
                                indicator.setText("Searching for REST Services in your project...");
                                loadServices(project);

                                ApplicationManager.getApplication().invokeLater(() -> {
                                    registerToolWindow(project);
                                });
                            }
                        })));
    }

    private void registerToolWindow(Project project) {
        String[] toolWindowIds = ToolWindowManager.getInstance(project).getToolWindowIds();

        boolean isToolWindowRegistered = Arrays.asList(toolWindowIds).stream().anyMatch(s -> s.equalsIgnoreCase(TOOL_WINDOW_ID));

        if (!isToolWindowRegistered) {
            ToolWindow toolWindow =
                    ToolWindowManager.getInstance(project).registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.RIGHT);

            toolWindow.getComponent().add(ui.getContentPanel());
            toolWindow.setTitle("REST Services");
            toolWindow.setIcon(AllIcons.CodeStyle.Gear);
            toolWindow.show(null);
        } else {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
            toolWindow.show(null);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getData(PROJECT);
        e.getPresentation().setVisible(project != null);
    }

    private void loadServices(Project project) {
        JTree servicesTree = ui.getServicesTree();

        servicesTree.setModel(null);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("REST Services");

        Map<String, List<RestServiceNode>> allServices = util.getAllServices(project);

        for (String moduleName : allServices.keySet()) {
            List<RestServiceNode> servicesByModule = allServices.get(moduleName);
            DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(moduleName);

            Map<HttpMethod, List<RestServiceNode>> groupedByHttpMethod = servicesByModule.stream()
                    .collect(groupingBy(RestServiceNode::getMethod));

            for (HttpMethod method : groupedByHttpMethod.keySet()) {
                List<RestServiceNode> services = groupedByHttpMethod.get(method);
                DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode(method);

                for (RestServiceNode service : services) {
                    DefaultMutableTreeNode restService = new DefaultMutableTreeNode(service);
                    methodNode.add(restService);
                }

                moduleNode.add(methodNode);

            }
            root.add(moduleNode);
        }

        DefaultTreeModel model = new DefaultTreeModel(root);
        ui.getServicesTree().setModel(model);

        servicesTree.getSelectionModel().setSelectionMode(SINGLE_TREE_SELECTION);
        servicesTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodeSelected = (DefaultMutableTreeNode) servicesTree.getLastSelectedPathComponent();

            if (nodeSelected == null) return;

            if (nodeSelected.getUserObject() instanceof RestServiceNode) {
                RestServiceNode service = (RestServiceNode) nodeSelected.getUserObject();
                OpenSourceUtil.navigate(true, service.getPsiMethod());
            }
        });
    }
}