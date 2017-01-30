package com.miguel.plugin.copyrest;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PsiElementUtil {

    public static final String REQUEST_MAPPING_QUALIFIED_NAME = "org.springframework.web.bind.annotation.RequestMapping";

    public String getParams(PsiParameterList parameterList, CopyRestUrlAction getParams) {
        StringBuilder query = new StringBuilder();
        List<String> params = new ArrayList<>();

        PsiParameter[] parameters = parameterList.getParameters();

        for (PsiParameter parameter : parameters) {
            PsiModifierList modifierList = parameter.getModifierList();

            if (elementContainsAnnotation(CopyRestUrlAction.REQUEST_PARAM, modifierList)) {
                params.add(parameter.getName());
            }
        }

        if (parameters.length > 0) {
            query.append("?");
            query.append(params.stream().map(s -> s + "=X").collect(Collectors.joining("&")));
        }

        return query.toString();
    }

    public boolean elementContainsAnnotation(String annotationName, PsiModifierList modifierList) {
        if (modifierList != null) {
            PsiAnnotation[] annotations = modifierList.getAnnotations();

            return Stream.of(annotations)
                    .map(a -> a.getQualifiedName())
                    .anyMatch(name -> name.equalsIgnoreCase(annotationName));

        }
        return false;
    }

    public String getAttributeValue(PsiNameValuePair[] attributes, String attributeName) {
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

    public String getRequestMappingValue(PsiModifierList modifierList, String attributeName, CopyRestUrlAction copyRestUrlAction) {

        if (modifierList != null) {
            PsiAnnotation[] annotations = modifierList.getAnnotations();

            for (PsiAnnotation annotation : annotations) {
                String qualifiedName = annotation.getQualifiedName();

                if (qualifiedName.equalsIgnoreCase(REQUEST_MAPPING_QUALIFIED_NAME)) {
                    PsiAnnotationParameterList parameterList = annotation.getParameterList();
                    PsiNameValuePair[] attributes = parameterList.getAttributes();

                    return getAttributeValue(attributes, attributeName);
                }
            }
        }
        return "";
    }
}