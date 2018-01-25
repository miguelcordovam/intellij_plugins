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
            PsiElement psiElement = expression.resolve();

            if(psiElement instanceof PsiField) {
                String fieldValue = ((PsiField) psiElement).computeConstantValue().toString();
                if (fieldValue.startsWith("PsiField:")) {
                    values.add(fieldValue.substring(9));
                } else {
                    values.add(fieldValue);
                }
            }
        } else if (value instanceof PsiLiteralExpression) {
            values.add(psiNameValuePair.getLiteralValue());
        } else if (value instanceof PsiArrayInitializerMemberValue) {
            PsiArrayInitializerMemberValue arrayValue = (PsiArrayInitializerMemberValue) value;

            for (PsiAnnotationMemberValue initializer : arrayValue.getInitializers()) {
                if (initializer instanceof PsiPolyadicExpression) {
                    values.add(getFullUrlFromExpression((PsiPolyadicExpression) initializer));
                } else if (initializer instanceof PsiReferenceExpression) {
                    PsiReferenceExpression expression = (PsiReferenceExpression) initializer;
                    PsiElement psiElement = expression.resolve();

                    if(psiElement instanceof PsiField) {
                        values.add(((PsiField) psiElement).computeConstantValue().toString());
                    } else {
                        values.add(expression.getText());
                    }
                } else {
                    values.add(initializer.getText().replaceAll("\\\"", ""));
                }
            }
        } else if (value instanceof PsiPolyadicExpression) {
            values.add(getFullUrlFromExpression((PsiPolyadicExpression) value));
        } else {
            values.add(value.getText());
        }
        return values;
    }

    private static String getFullUrlFromExpression(PsiPolyadicExpression polyadicExpression) {
        PsiExpression[] operands = polyadicExpression.getOperands();

        StringBuilder fullUrl = new StringBuilder("");

        for (PsiExpression operand : operands) {
            if (operand instanceof PsiReferenceExpression) {
                PsiElement element = ((PsiReferenceExpression) operand).resolve();
                if (element instanceof PsiField) {
                    fullUrl.append(((PsiField) element).computeConstantValue().toString());
                }
            } else if (operand instanceof PsiLiteralExpression) {
                fullUrl.append(((PsiLiteralExpression) operand).getValue());
            }
        }

        return fullUrl.toString();
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