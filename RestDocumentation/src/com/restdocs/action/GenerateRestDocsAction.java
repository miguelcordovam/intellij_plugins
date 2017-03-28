package com.restdocs.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.restdocs.action.util.FileUtil;
import com.restdocs.action.util.PsiElementUtil;
import com.restdocs.action.util.ServicesUtil;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT;

public class GenerateRestDocsAction extends AnAction {


    private ServicesUtil servicesUtil = new ServicesUtil();
    private FileUtil fileUtil = new FileUtil();
    private PsiElementUtil psiElementUtil = new PsiElementUtil();

    @Override
    public void actionPerformed(AnActionEvent e) {

        // TODO create export REST services as html file option on toolwindow
//        Project project = e.getProject();
//
//        fileUtil.createFile(project, "restDocumentation.html", servicesUtil.getAllServicesByModule(project));
    }

    @Override
    public void update(AnActionEvent e) {

        Project project = e.getData(PROJECT);
        Editor editor = e.getData(EDITOR);

        e.getPresentation().setVisible(project != null);
    }


}
