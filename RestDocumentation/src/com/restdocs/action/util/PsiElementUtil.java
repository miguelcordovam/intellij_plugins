package com.restdocs.action.util;

import com.intellij.psi.*;
import com.restdocs.action.common.SpringAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PsiElementUtil {

    private static List<String> getValues(PsiNameValuePair psiNameValuePair) {
        List<String> values = new ArrayList<>();
        PsiAnnotationMemberValue value = psiNameValuePair.getValue();

        if (value instanceof PsiReferenceExpression) {
            PsiReferenceExpression expression = (PsiReferenceExpression) value;
            values.add(expression.getText());
        } else if (value instanceof PsiLiteralExpression) {
            values.add(psiNameValuePair.getLiteralValue());
        } else if (value instanceof PsiArrayInitializerMemberValue) {
            PsiArrayInitializerMemberValue arrayValue = (PsiArrayInitializerMemberValue) value;

            for (PsiAnnotationMemberValue initializer : arrayValue.getInitializers()) {
                values.add(initializer.getText().replaceAll("\\\"", ""));
            }
        }
        return values;
    }

    private static List<String> getAttributeValue(PsiNameValuePair[] attributes, String attributeName) {
        List<String> values = new ArrayList<>();

        if (attributes != null && attributes.length == 1) {
            PsiNameValuePair psiNameValuePair = attributes[0];

            if (psiNameValuePair.getName() != null && psiNameValuePair.getName().equalsIgnoreCase(attributeName) ||
                    psiNameValuePair.getName() == null &&
                            ("value".equalsIgnoreCase(attributeName) || "path".equalsIgnoreCase(attributeName))) {
                return getValues(psiNameValuePair);
            }
        } else if (attributes != null && attributes.length > 1) {
            Optional<PsiNameValuePair> psiNameValuePair =
                    Stream.of(attributes)
                            .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase(attributeName))
                            .findFirst();

            if (psiNameValuePair.isPresent()) {
                return getValues(psiNameValuePair.get());
            }
        }

        return values;
    }

    public static List<String> getAnnotationValue(PsiModifierList modifierList, String attributeName, SpringAnnotations springAnnotation) {
        List<String> values = new ArrayList<>();

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
        return values;
    }

    public static List<String> getUrls(PsiModifierList modifierList, SpringAnnotations springAnnotation) {

        List<String> withPath = getAnnotationValue(modifierList, "path", springAnnotation);
        List<String> withValue = getAnnotationValue(modifierList, "value", springAnnotation);

        if (withPath.isEmpty() && !withValue.isEmpty()) {
            return withValue;
        } else if (!withPath.isEmpty() && withValue.isEmpty()) {
            return withPath;
        }

        return withValue;
    }
}