package com.copyresturl.util;

import com.copyresturl.common.SpringAnnotations;
import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.copyresturl.common.SpringAnnotations.REQUEST_PARAM;

public class PsiElementUtil {

    private String getAttributeValue(PsiNameValuePair[] attributes, String attributeName) {
        if (attributes != null && attributes.length == 1) {
            PsiNameValuePair attribute = attributes[0];
            if (attribute.getName() != null && attribute.getName().equalsIgnoreCase(attributeName)) {
                return attribute.getValue().getText();
            } else if (attribute.getLiteralValue() != null && "value".equalsIgnoreCase(attributeName)) {
                return attribute.getLiteralValue();
            }

        } else if (attributes != null && attributes.length > 1) {
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

    public String createQueryWithParameters(PsiParameterList parameterList) {
        StringBuilder query = new StringBuilder();
        List<String> params = new ArrayList<>();

        PsiParameter[] parameters = parameterList.getParameters();

        for (PsiParameter parameter : parameters) {
            PsiModifierList modifierList = parameter.getModifierList();

            if (containsSpringAnnotation(REQUEST_PARAM, modifierList)) {
                String paramValue = getAnnotationValue(modifierList, "value", REQUEST_PARAM);

                if (paramValue.isEmpty()) {
                    params.add(parameter.getName());
                } else {
                    params.add(paramValue);
                }
            }
        }

        if (parameters.length > 0) {
            query.append("?");
            query.append(params.stream().map(s -> s + "=X").collect(Collectors.joining("&")));
        }

        return query.toString();
    }

    public boolean containsSpringAnnotation(SpringAnnotations springAnnotation, PsiModifierList modifierList) {
        if (modifierList != null) {
            PsiAnnotation[] annotations = modifierList.getAnnotations();

            return Stream.of(annotations)
                    .map(a -> a.getQualifiedName())
                    .anyMatch(name -> name.equalsIgnoreCase(springAnnotation.getQualifiedName()));
        }

        return false;
    }

    public String getAnnotationValue(PsiModifierList modifierList, String attributeName, SpringAnnotations springAnnotation) {

        if (modifierList != null) {
            PsiAnnotation[] annotations = modifierList.getAnnotations();

            for (PsiAnnotation psiAnnotation : annotations) {
                String qualifiedName = psiAnnotation.getQualifiedName();

                if (qualifiedName.equalsIgnoreCase(springAnnotation.getQualifiedName())) {
                    PsiAnnotationParameterList parameterList = psiAnnotation.getParameterList();
                    PsiNameValuePair[] attributes = parameterList.getAttributes();

                    return getAttributeValue(attributes, attributeName);
                }
            }
        }
        return "";
    }
}