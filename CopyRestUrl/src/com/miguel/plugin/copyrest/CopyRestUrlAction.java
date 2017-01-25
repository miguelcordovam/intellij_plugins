package com.miguel.plugin.copyrest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

import java.awt.datatransfer.StringSelection;
import java.util.Optional;
import java.util.stream.Stream;

public class CopyRestUrlAction extends AnAction {

    public static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    public static final String CONTROLLER = "org.springframework.stereotype.Controller";

    @Override
    public void actionPerformed(AnActionEvent e) {

        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);

        PsiMethod psiMethod = (PsiMethod) psiElement;
        PsiClass psiClass = psiMethod.getContainingClass();

        PsiModifierList classModifierList = psiClass.getModifierList();
        PsiModifierList methodModifierList = psiMethod.getModifierList();

        String classUrl = getRequestMappingValue(classModifierList);
        String methodUrl = getRequestMappingValue(methodModifierList);

        CopyPasteManager.getInstance().setContents(new StringSelection(classUrl + methodUrl));
    }

    private String getRequestMappingValue(PsiModifierList modifierList) {

        if (modifierList != null) {
            PsiAnnotation[] annotations = modifierList.getAnnotations();

            for (PsiAnnotation annotation : annotations) {
                String qualifiedName = annotation.getQualifiedName();

                if (qualifiedName.equalsIgnoreCase("org.springframework.web.bind.annotation.RequestMapping")) {
                    PsiAnnotationParameterList parameterList = annotation.getParameterList();
                    PsiNameValuePair[] attributes = parameterList.getAttributes();

                    return getUrlFromValue(attributes);
                }
            }
        }
        return "";
    }

    private String getUrlFromValue(PsiNameValuePair[] attributes) {
        if (attributes.length == 1) {
            return attributes[0].getLiteralValue();
        } else if (attributes.length > 1) {
            Optional<PsiNameValuePair> psiNameValuePair =
                    Stream.of(attributes)
                            .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase("value"))
                            .findFirst();

            if (psiNameValuePair.isPresent()) {
                return psiNameValuePair.get().getLiteralValue();
            }
        }
        return "";
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

            available = elementContainsAnnotation(REQUEST_MAPPING, psiMethod.getModifierList()) &&
                    elementContainsAnnotation(CONTROLLER, psiClass.getModifierList());
        }

        e.getPresentation().setVisible(project != null && editor != null && available);
    }

    private boolean elementContainsAnnotation(String annotationName, PsiModifierList modifierList) {
        if (modifierList != null) {
            PsiAnnotation[] annotations = modifierList.getAnnotations();

            return Stream.of(annotations)
                    .map(a -> a.getQualifiedName())
                    .anyMatch(name -> name.equalsIgnoreCase(annotationName));

        }
        return false;
    }
}