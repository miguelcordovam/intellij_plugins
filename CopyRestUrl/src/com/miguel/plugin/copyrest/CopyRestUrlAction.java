package com.miguel.plugin.copyrest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import java.awt.datatransfer.StringSelection;

import static com.miguel.plugin.copyrest.PsiElementUtil.REQUEST_MAPPING_QUALIFIED_NAME;

public class CopyRestUrlAction extends AnAction {

    public static final String CONTROLLER = "org.springframework.stereotype.Controller";
    public static final String REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
    public static final String LOCALHOST = "http://localhost:";
    public static final String DEFAULT_PORT = "8080";
    public static final String REQUEST_METHOD_GET = "RequestMethod.GET";
    public static final String APPLICATION_PROPERTIES = "application.properties";
    public static final String SERVER_PORT = "server.port";
    public static final String SERVER_CONTEXT_PATH = "server.contextPath";
    public static final String VALUE = "value";
    public static final String METHOD = "method";

    private PropertiesUtil propertiesUtil = new PropertiesUtil();
    private PsiElementUtil psiElementUtil = new PsiElementUtil();

    @Override
    public void actionPerformed(AnActionEvent e) {

        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);

        String queryList = "";
        String port = "";
        String contextPath = "";

        PsiMethod psiMethod = (PsiMethod) psiElement;
        PsiClass psiClass = psiMethod.getContainingClass();

        PsiModifierList classModifierList = psiClass.getModifierList();
        PsiModifierList methodModifierList = psiMethod.getModifierList();

        String classUrl = psiElementUtil.getRequestMappingValue(classModifierList, VALUE, this);
        String methodUrl = psiElementUtil.getRequestMappingValue(methodModifierList, VALUE, this);

        String httpMethod = psiElementUtil.getRequestMappingValue(methodModifierList, METHOD, this);

        if (httpMethod.equalsIgnoreCase("GET") || httpMethod.equalsIgnoreCase(REQUEST_METHOD_GET)) {
             queryList = psiElementUtil.getParams(psiMethod.getParameterList(), this);
        }

        PsiFile[] filesByName = FilenameIndex.getFilesByName(e.getProject(), APPLICATION_PROPERTIES,
                GlobalSearchScope.allScope(e.getProject()));

        if (filesByName.length > 0) {
            PsiFile psiFile = filesByName[0];

            port = propertiesUtil.getPropertyValue(psiFile.getText(), SERVER_PORT);
            contextPath = propertiesUtil.getPropertyValue(psiFile.getText(), SERVER_CONTEXT_PATH);
        }

        StringBuilder url = new StringBuilder();
        url.append(LOCALHOST);
        url.append(port.isEmpty() ? DEFAULT_PORT : port);
        url.append(contextPath);
        url.append(classUrl);
        url.append(methodUrl);
        url.append(queryList);

        CopyPasteManager.getInstance().setContents(new StringSelection(url.toString()));
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);

        boolean available = false;

        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            PsiClass psiClass = psiMethod.getContainingClass();

            available = psiElementUtil.elementContainsAnnotation(REQUEST_MAPPING_QUALIFIED_NAME, psiMethod.getModifierList()) &&
                    psiElementUtil.elementContainsAnnotation(CONTROLLER, psiClass.getModifierList());
        }

        e.getPresentation().setVisible(project != null && editor != null && available);
    }
}