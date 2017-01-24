package com.intellij.codeInspection;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ForStatementConditionFix implements LocalQuickFix {

    private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.ForStatementConditionFix");

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Condition in for statement is not correct";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {

        try {

        } catch (IncorrectOperationException e) {
            LOG.error(e);
        }

        PsiBinaryExpression binaryExpression = (PsiBinaryExpression) descriptor.getPsiElement();
        PsiExpression lExpr = binaryExpression.getLOperand();
        PsiExpression rExpr = binaryExpression.getROperand();

        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

        PsiBinaryExpression lessThanExpression =
                (PsiBinaryExpression) factory.createExpressionFromText("a < b", null);

        lessThanExpression.getLOperand().replace(rExpr);
        lessThanExpression.getROperand().replace(lExpr);

        binaryExpression.replace(lessThanExpression);
    }
}
