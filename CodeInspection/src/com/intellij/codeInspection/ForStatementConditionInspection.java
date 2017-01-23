package com.intellij.codeInspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class ForStatementConditionInspection extends BaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.ForStatementConditionInspection");

    @NonNls
    private static final String DESCRIPTION_TEMPLATE =
            InspectionsBundle.message("inspection.comparing.references.problem.descriptor");

    @NotNull
    public String getDisplayName() {
        return "For condition is not clear";
    }

    @NotNull
    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String getShortName() {
        return "ForStatementCondition";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
            }

            @Override
            public void visitForStatement(PsiForStatement statement) {
                super.visitForStatement(statement);

                PsiExpression expression = statement.getCondition();

                PsiElement[] children = expression.getChildren();

                List<PsiElement> onlyChildren = new ArrayList<>();

                for (PsiElement element : children) {
                    if (!(element instanceof PsiWhiteSpace)) {
                        onlyChildren.add(element);
                    }
                }

                PsiElement left = onlyChildren.get(0);
                PsiElement operand = onlyChildren.get(1);
                PsiElement right = onlyChildren.get(2);

                if (onlyChildren.size() == 3
                        && left instanceof PsiLiteralExpression
                        && operand instanceof PsiJavaToken
                        && ((PsiJavaToken) operand).getTokenType() == JavaTokenType.GT
                        && right instanceof PsiReferenceExpression) {
                    holder.registerProblem(expression, DESCRIPTION_TEMPLATE, new ForStatementConditionFix());
                }
            }
        };
    }

    public boolean isEnabledByDefault() {
        return true;
    }
}
