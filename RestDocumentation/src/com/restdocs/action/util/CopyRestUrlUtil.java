package com.restdocs.action.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.restdocs.action.common.HttpMethod;
import com.restdocs.action.common.RestServiceNode;

import java.util.ArrayList;
import java.util.List;

import static com.restdocs.action.common.SpringAnnotations.REQUEST_PARAM;
import static java.util.stream.Collectors.joining;

public class CopyRestUrlUtil {

    private static final String LOCALHOST = "http://localhost:";
    private static final String DEFAULT_PORT = "8080";
    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String SERVER_PORT = "server.port";
    private static final String SERVER_CONTEXT_PATH = "server.contextPath";

    private Project project;
    private PropertiesUtil propertiesUtil = new PropertiesUtil();
    private PsiElementUtil psiElementUtil = new PsiElementUtil();

    public CopyRestUrlUtil(Project project) {
        this.project = project;
    }

    public String getFullUrl(String partialUrl, RestServiceNode serviceNode) {
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, APPLICATION_PROPERTIES,
                GlobalSearchScope.allScope(project));

        String port = "";
        String contextPath = "";

        if (filesByName.length > 0) {
            PsiFile psiFile = filesByName[0];

            port = propertiesUtil.getPropertyValue(psiFile.getText(), SERVER_PORT);
            contextPath = propertiesUtil.getPropertyValue(psiFile.getText(), SERVER_CONTEXT_PATH);
        }

        String queryList = "";

        if (serviceNode.getMethod() == HttpMethod.GET) {
            queryList = createQueryWithParameters(serviceNode.getPsiMethod().getParameterList());
        }

        StringBuilder url = new StringBuilder();
        url.append(LOCALHOST);
        url.append(port.isEmpty() ? DEFAULT_PORT : port);
        url.append(contextPath);
        url.append(partialUrl);
        url.append(queryList);

        return url.toString();
    }

    public String createQueryWithParameters(PsiParameterList parameterList) {
        StringBuilder query = new StringBuilder();
        List<String> params = new ArrayList<>();

        PsiParameter[] parameters = parameterList.getParameters();

        for (PsiParameter parameter : parameters) {
            PsiModifierList modifierList = parameter.getModifierList();

            if (psiElementUtil.containsSpringAnnotation(REQUEST_PARAM, modifierList)) {
                List<String> paramValue = psiElementUtil.getAnnotationValue(modifierList, "value", REQUEST_PARAM);

                if (paramValue.isEmpty()) {
                    params.add(parameter.getName());
                } else {
                    params.add(paramValue.get(0));
                }
            }
        }

        if (params.size() > 0) {
            query.append("?");
            query.append(params.stream().map(s -> s + "=X").collect(joining("&")));
        }

        return query.toString();
    }
}
