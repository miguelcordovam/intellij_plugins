package com.miguel.plugin.copyrest;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CopyRestUrlAction extends AnAction {

    public static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    public static final String CONTROLLER = "org.springframework.stereotype.Controller";
    public static final String REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";

    @Override
    public void actionPerformed(AnActionEvent e) {

        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);

        PsiMethod psiMethod = (PsiMethod) psiElement;
        PsiClass psiClass = psiMethod.getContainingClass();

        PsiModifierList classModifierList = psiClass.getModifierList();
        PsiModifierList methodModifierList = psiMethod.getModifierList();

        String classUrl = getRequestMappingValue(classModifierList, "value");
        String methodUrl = getRequestMappingValue(methodModifierList, "value");

        String httpMethod = getRequestMappingValue(methodModifierList, "method");
        String queryList = "";

        if (httpMethod.equalsIgnoreCase("GET") || httpMethod.equalsIgnoreCase("RequestMethod.GET")) {
             queryList = getParams(psiMethod.getParameterList());
        }

        CopyPasteManager.getInstance().setContents(new StringSelection(classUrl + methodUrl + queryList));
    }

    private String getParams(PsiParameterList parameterList) {
        StringBuilder query = new StringBuilder();
        List<String> params = new ArrayList<>();

        PsiParameter[] parameters = parameterList.getParameters();

        for (PsiParameter parameter : parameters) {
            PsiModifierList modifierList = parameter.getModifierList();

            if(elementContainsAnnotation(REQUEST_PARAM, modifierList)) {
                params.add(parameter.getName());
            }
        }

        if (parameters.length > 0) {
            query.append("?");
            query.append(params.stream().map(s -> s + "=X").collect(Collectors.joining("&")));
        }

        return query.toString();
    }

    private String getRequestMappingValue(PsiModifierList modifierList, String attributeName) {

        if (modifierList != null) {
            PsiAnnotation[] annotations = modifierList.getAnnotations();

            for (PsiAnnotation annotation : annotations) {
                String qualifiedName = annotation.getQualifiedName();

                if (qualifiedName.equalsIgnoreCase("org.springframework.web.bind.annotation.RequestMapping")) {
                    PsiAnnotationParameterList parameterList = annotation.getParameterList();
                    PsiNameValuePair[] attributes = parameterList.getAttributes();

                    return getAttributeValue(attributes, attributeName);
                }
            }
        }
        return "";
    }

    private String getAttributeValue(PsiNameValuePair[] attributes, String attributeName) {
        if (attributes.length == 1) {
            return attributes[0].getLiteralValue();
        } else if (attributes.length > 1) {
            Optional<PsiNameValuePair> psiNameValuePair =
                    Stream.of(attributes)
                            .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase(attributeName))
                            .findFirst();

            if (psiNameValuePair.isPresent()) {
                if (psiNameValuePair.get().getLiteralValue() != null) {
                    return psiNameValuePair.get().getLiteralValue();
                } else {
                    return psiNameValuePair.get().getValue().getText();
                }
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