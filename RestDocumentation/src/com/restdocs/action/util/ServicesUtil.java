package com.restdocs.action.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.restdocs.action.common.HttpMethod;
import com.restdocs.action.common.RestServiceNode;

import java.util.*;
import java.util.regex.Pattern;

import static com.restdocs.action.common.HttpMethod.*;
import static com.restdocs.action.common.SpringAnnotations.*;
import static com.restdocs.action.util.PsiElementUtil.getAnnotationValue;
import static com.restdocs.action.util.PsiElementUtil.getUrls;
import static com.restdocs.action.util.SpringUtil.containsSpringAnnotation;
import static com.restdocs.action.util.SpringUtil.isRestMethod;

public class ServicesUtil {

    public static final String VALUE = "value";
    public static final String METHOD = "method";

    private static boolean isClassControllerAnnotated(PsiClass psiClass) {
        return containsSpringAnnotation(CONTROLLER, psiClass.getModifierList()) ||
                containsSpringAnnotation(REST_CONTROLLER, psiClass.getModifierList());
    }

    private static boolean isMethodRestAnnotated(PsiMethod method) {
        return isRestMethod(method.getModifierList());
    }

    private static List<String> getRESTUrls(PsiMethod psiMethod) {

        List<String> restUrls = new ArrayList<>();

        List<String> classUrls = getAnnotationValue(psiMethod.getContainingClass().getModifierList(), VALUE, REQUEST_MAPPING_QUALIFIED_NAME);
        PsiModifierList methodModifierList = psiMethod.getModifierList();

        List<String> methodUrls = new ArrayList<>();

        if (containsSpringAnnotation(REQUEST_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrls = getUrls(methodModifierList, REQUEST_MAPPING_QUALIFIED_NAME);
        } else if (containsSpringAnnotation(GET_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrls = getUrls(methodModifierList, GET_MAPPING_QUALIFIED_NAME);
        } else if (containsSpringAnnotation(POST_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrls = getUrls(methodModifierList, POST_MAPPING_QUALIFIED_NAME);
        } else if (containsSpringAnnotation(PATCH_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrls = getUrls(methodModifierList, PATCH_MAPPING_QUALIFIED_NAME);
        } else if (containsSpringAnnotation(DELETE_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrls = getUrls(methodModifierList, DELETE_MAPPING_QUALIFIED_NAME);
        } else if (containsSpringAnnotation(PUT_MAPPING_QUALIFIED_NAME, methodModifierList)) {
            methodUrls = getUrls(methodModifierList, PUT_MAPPING_QUALIFIED_NAME);
        }

        if (classUrls != null && classUrls.size() > 0) {
            for (String classUrl : classUrls) {
                for (String methodUrl : methodUrls) {
                    restUrls.add(classUrl + methodUrl);
                }
            }
        } else {
            for (String methodUrl : methodUrls) {
                restUrls.add(methodUrl);
            }
        }

        return restUrls;
    }

    public static Map<String, List<RestServiceNode>> getAllServicesByModule(Project project) {
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
                                if (isMethodRestAnnotated(method)) {
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

    private static List<RestServiceNode> createRestServices(List<PsiMethod> services) {
        List<RestServiceNode> restServices = new ArrayList<>();

        for (PsiMethod method : services) {

            List<String> restUrls = getRESTUrls(method);
            List<String> httpMethods = new ArrayList<>();
            PsiModifierList methodModifierList = method.getModifierList();

            if (containsSpringAnnotation(REQUEST_MAPPING_QUALIFIED_NAME, methodModifierList)) {
                List<String> methodValue = getAnnotationValue(methodModifierList, METHOD, REQUEST_MAPPING_QUALIFIED_NAME);
                if (!methodValue.isEmpty()) {
                    httpMethods.addAll(methodValue);
                }
            } else if (containsSpringAnnotation(GET_MAPPING_QUALIFIED_NAME, methodModifierList)) {
                httpMethods.add(GET.toString());
            } else if (containsSpringAnnotation(POST_MAPPING_QUALIFIED_NAME, methodModifierList)) {
                httpMethods.add(POST.toString());
            } else if (containsSpringAnnotation(PATCH_MAPPING_QUALIFIED_NAME, methodModifierList)) {
                httpMethods.add(PATCH.toString());
            } else if (containsSpringAnnotation(DELETE_MAPPING_QUALIFIED_NAME, methodModifierList)) {
                httpMethods.add(DELETE.toString());
            } else if (containsSpringAnnotation(PUT_MAPPING_QUALIFIED_NAME, methodModifierList)) {
                httpMethods.add(PUT.toString());
            }

            if (httpMethods.isEmpty()) {
                List<String> classMethods = getAnnotationValue(method.getContainingClass().getModifierList(), METHOD, REQUEST_MAPPING_QUALIFIED_NAME);

                if (classMethods.size() >= 1 && !classMethods.get(0).isEmpty()) {
                    httpMethods.clear();
                    httpMethods.addAll(classMethods);
                } else {
                    httpMethods.add(GET.toString());
                }
            }

            for (String url : restUrls) {
                for (String met : httpMethods) {
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

    private static HttpMethod getHttpMethod(String method) {
        if (method.isEmpty()) {
            return GET;
        }

        String[] split = method.split("\\.");

        if (split.length > 0) {
            return HttpMethod.valueOf(split[split.length - 1].toUpperCase());
        }

        return HttpMethod.valueOf(method);
    }

    public static boolean matches(String serviceUrl, String serviceQuery) {
        StringBuilder pattern = new StringBuilder("^.*");
        pattern.append(serviceQuery.toLowerCase().trim().replace("/", ".*"));
        pattern.append(".*$");

        return Pattern.matches(pattern.toString(), serviceUrl.toLowerCase());
    }
}