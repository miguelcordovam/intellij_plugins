package com.restdocs.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerAdapter;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.util.OpenSourceUtil;
import com.restdocs.action.common.HttpMethod;
import com.restdocs.action.common.RestService;
import com.restdocs.action.util.Util;
import com.restdocs.toolwindow.ShowRestServicesForm;
import com.restdocs.widgets.StatusBarLoadingWidget;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT;
import static java.util.stream.Collectors.groupingBy;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class ShowRestServicesAction extends AnAction {

    public static final String TOOL_WINDOW_ID = "Show Rest Services";
    private Util util = new Util();
    private boolean reloadServices = false;
    private ShowRestServicesForm ui = new ShowRestServicesForm();

    @Override
    public void actionPerformed(AnActionEvent e) {

        Project project = e.getData(CommonDataKeys.PROJECT);

        String[] toolWindowIds = ToolWindowManager.getInstance(project).getToolWindowIds();

        boolean isRegistered = Arrays.asList(toolWindowIds).stream().anyMatch(s -> s.equalsIgnoreCase(TOOL_WINDOW_ID));

        if (!isRegistered) {
            ToolWindow toolWindow =
                    ToolWindowManager.getInstance(project).registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.RIGHT);

            toolWindow.getComponent().add(ui.getContentPanel());
            toolWindow.setTitle("REST Services");
            toolWindow.show(() -> loadServices(project));

            addListeners(toolWindow, project);
        } else {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
            toolWindow.show(() -> loadServices(project));
        }
    }

    private void addListeners(ToolWindow myToolWindow, final Project project) {

        ToolWindowManagerEx manager = ToolWindowManagerEx.getInstanceEx(project);

        ToolWindowManagerAdapter toolWindowManagerListener = new ToolWindowManagerAdapter() {
            @Override
            public void stateChanged() {
                if (!myToolWindow.isVisible() && !reloadServices) {
                    manager.removeToolWindowManagerListener(this);
                    reloadServices = true;
                }
            }
        };

        FocusListener listener = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (reloadServices) {
//                    WindowManager.getInstance().getStatusBar(project).addWidget(new StatusBarLoadingWidget(), "before Position");
                    SwingUtilities.invokeLater(() -> loadServices(project));
                    reloadServices = false;
                    manager.addToolWindowManagerListener(toolWindowManagerListener);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!myToolWindow.isVisible()) {
                    reloadServices = true;
                }
            }
        };

        ui.getServicesTree().addFocusListener(listener);

        if (reloadServices) {
            SwingUtilities.invokeLater(() -> loadServices(project));
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

        ui.getContentPanel().setVisible(false);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Services");

        Map<String, List<RestService>> allServices = util.getAllServices(project);

        for (String moduleName : allServices.keySet()) {
            List<RestService> servicesByModule = allServices.get(moduleName);
            DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode(moduleName);

            Map<HttpMethod, List<RestService>> groupedByHttpMethod =  servicesByModule.stream()
                    .collect(groupingBy(RestService::getMethod));

            for (HttpMethod method : groupedByHttpMethod.keySet()) {
                List<RestService> services = groupedByHttpMethod.get(method);
                DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode(method);

                for (RestService service : services) {
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

            if (nodeSelected.getUserObject() instanceof RestService) {
                RestService service = (RestService) nodeSelected.getUserObject();
                OpenSourceUtil.navigate(true, service.getPsiMethod());
            }
        });

        ui.getContentPanel().setVisible(true);
    }
}