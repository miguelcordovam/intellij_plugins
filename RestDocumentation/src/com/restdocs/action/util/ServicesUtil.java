package com.restdocs.action.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.restdocs.action.common.HttpMethod;
import com.restdocs.action.common.RestServiceNode;

import java.util.*;
import java.util.regex.Pattern;

import static com.restdocs.action.common.SpringAnnotations.CONTROLLER;
import static com.restdocs.action.common.SpringAnnotations.REQUEST_MAPPING_QUALIFIED_NAME;

public class ServicesUtil {

    public static final String VALUE = "value";
    public static final String METHOD = "method";
    private PsiElementUtil psiElementUtil = new PsiElementUtil();

    private boolean isClassControllerAnnotated(PsiClass psiClass) {
        return psiElementUtil.containsSpringAnnotation(CONTROLLER, psiClass.getModifierList());
    }

    private boolean isMethodRequestMappingAnnotated(PsiMethod method) {
        return psiElementUtil.containsSpringAnnotation(REQUEST_MAPPING_QUALIFIED_NAME, method.getModifierList());
    }

    private List<String> getRESTUrls(PsiMethod psiMethod) {

        List<String> restUrls = new ArrayList<>();

        List<String> classUrls = psiElementUtil.getAnnotationValue(psiMethod.getContainingClass().getModifierList(), VALUE, REQUEST_MAPPING_QUALIFIED_NAME);
        List<String> methodUrls = psiElementUtil.getAnnotationValue(psiMethod.getModifierList(), VALUE, REQUEST_MAPPING_QUALIFIED_NAME);

        for (String classUrl : classUrls) {
            for (String methodUrl : methodUrls) {
                restUrls.add(classUrl + methodUrl);
            }
        }

        return restUrls;
    }

    public Map<String, List<RestServiceNode>> getAllServicesByModule(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();

        Map<String, List<RestServiceNode>> result = new HashMap<>();

        for (Module module : modules) {

            ApplicationManager.getApplication().runReadAction(new Runnable() {

                @Override
                public void run() {
                    Collection<PsiClass> all = AllClassesSearch.search(GlobalSearchScope.moduleScope(module), project).findAll();

                    List<PsiMethod> services = new ArrayList<>();

                    for (PsiClass psiClass : all) {
                        if (isClassControllerAnnotated(psiClass)) {
                            PsiMethod[] methods = psiClass.getMethods();

                            for (PsiMethod method : methods) {
                                if (isMethodRequestMappingAnnotated(method)) {
                                    services.add(method);
                                }
                            }
                        }
                    }

                    List<RestServiceNode> restServices = createRestServices(services);

                    if (restServices.size() > 0) {
                        result.put(module.getName(), restServices);
                    }
                }
            });
        }

        return result;
    }

    private List<RestServiceNode> createRestServices(List<PsiMethod> services) {
        List<RestServiceNode> restServices = new ArrayList<>();

        for (PsiMethod method : services) {

            List<String> restUrls = getRESTUrls(method);
            List<String> methods = psiElementUtil.getAnnotationValue(method.getModifierList(), METHOD, REQUEST_MAPPING_QUALIFIED_NAME);

            if (methods.size() == 1 && methods.get(0).isEmpty()) {
                List<String> classMethods = psiElementUtil.getAnnotationValue(method.getContainingClass().getModifierList(), METHOD, REQUEST_MAPPING_QUALIFIED_NAME);

                if (classMethods.size() >= 1 && !classMethods.get(0).isEmpty()) {
                    methods.clear();
                    methods.addAll(classMethods);
                }
            }

            for (String url : restUrls) {
                for (String met : methods) {
                    RestServiceNode restServiceNode = new RestServiceNode();
                    restServiceNode.setUrl(url);
                    restServiceNode.setMethod(getHttpMethod(met));
                    restServiceNode.setName(method.getName());
                    restServiceNode.setPsiMethod(method);
                    restServiceNode.setPsiClass(method.getContainingClass());

                    restServices.add(restServiceNode);
                }
            }
        }
        return restServices;
    }

    private HttpMethod getHttpMethod(String method) {
        if (method.isEmpty()) {
            return HttpMethod.GET;
        }

        String[] split = method.split("\\.");

        if (split.length > 0) {
            return HttpMethod.valueOf(split[split.length - 1].toUpperCase());
        }

        return HttpMethod.valueOf(method);
    }

    public boolean matches (String serviceUrl, String serviceQuery) {
        StringBuilder pattern = new StringBuilder("^.*");
        pattern.append(serviceQuery.toLowerCase().trim().replace("/", ".*"));
        pattern.append(".*$");

        return Pattern.matches(pattern.toString(), serviceUrl.toLowerCase());
    }
}