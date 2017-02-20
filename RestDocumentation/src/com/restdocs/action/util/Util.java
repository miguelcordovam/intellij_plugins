package com.restdocs.action.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.restdocs.action.common.HttpMethod;
import com.restdocs.action.common.RestService;

import java.util.*;

import static com.restdocs.action.common.SpringAnnotations.CONTROLLER;
import static com.restdocs.action.common.SpringAnnotations.REQUEST_MAPPING_QUALIFIED_NAME;

public class Util {

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

    public Map<String, List<RestService>> getAllServices(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();

        Map<String, List<RestService>> result = new HashMap<>();

        for (Module module : modules) {
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

            List<RestService> restServices = createRestServices(services);

            if (restServices.size() > 0) {
                result.put(module.getName(), restServices);
            }
        }

        return result;
    }

    private List<RestService> createRestServices (List<PsiMethod> services) {
        List<RestService> restServices = new ArrayList<>();

        for (PsiMethod method : services) {

            List<String> restUrls = getRESTUrls(method);
            List<String> methods = psiElementUtil.getAnnotationValue(method.getModifierList(), METHOD, REQUEST_MAPPING_QUALIFIED_NAME);

            if (methods.size() == 1 && methods.get(0).isEmpty()) {
                List<String> classMethods = psiElementUtil.getAnnotationValue(method.getContainingClass().getModifierList(), METHOD, REQUEST_MAPPING_QUALIFIED_NAME);

                if(classMethods.size() >=1 && !classMethods.get(0).isEmpty()) {
                    methods.clear();
                    methods.addAll(classMethods);
                }
            }

            for (String url: restUrls) {
                for(String met: methods) {
                    RestService restService = new RestService();
                    restService.setUrl(url);
                    restService.setMethod(getHttpMethod(met));
                    restService.setName(method.getName());
                    restService.setPsiMethod(method);
                    restService.setPsiClass(method.getContainingClass());

                    restServices.add(restService);
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
}