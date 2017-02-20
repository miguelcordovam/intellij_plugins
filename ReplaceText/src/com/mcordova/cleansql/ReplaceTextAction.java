package com.mcordova.cleansql;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.Query;

import java.awt.datatransfer.StringSelection;
import java.util.Collection;

public class ReplaceTextAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);

        if (psiElement instanceof PsiField) {
            PsiField field = (PsiField) psiElement;
            PsiElement[] children = field.getInitializer().getChildren();

            StringBuilder sqlQuery = new StringBuilder("");

            for (PsiElement child : children) {
                if (child instanceof PsiLiteralExpression) {
                    PsiLiteralExpression expression = (PsiLiteralExpression) child;
                    sqlQuery.append(expression.getText().replaceAll("\\+|\"", "") + "\n");
                }
            }

            CopyPasteManager.getInstance().setContents(new StringSelection(sqlQuery.toString()));
        }
    }

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        boolean isField = element instanceof PsiField;

        boolean isFieldString = false;
        boolean isInitializedCorrectly = false;

        if (isField) {
            PsiField field = (PsiField) element;
            isFieldString = field.getType().getPresentableText().equalsIgnoreCase("String");
            isInitializedCorrectly = field.getInitializer() != null
                    && field.getInitializer().getText() != null
                    && field.getInitializer().getType() != null
                    && field.getInitializer().getType().getPresentableText().equalsIgnoreCase("String");
        }

        final Editor editor = e.getData(CommonDataKeys.EDITOR);

        e.getPresentation().setVisible(project != null && editor !=
                null && isField && isInitializedCorrectly && isFieldString);
    }
}
