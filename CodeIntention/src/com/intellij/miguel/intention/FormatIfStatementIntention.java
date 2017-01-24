package com.intellij.miguel.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeStyle.CodeStyleFacade;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import org.apache.xmlbeans.impl.xb.ltgfmt.Code;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NonNls
public class FormatIfStatementIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    @NotNull
    @Override
    public String getText() {
        return "Format if statement from one line to many";
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiElement element)
            throws IncorrectOperationException {

        PsiElement conditionalExpression = element.getParent();
        CodeStyleManager.getInstance(project).reformat(conditionalExpression);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement element) {
        if (element == null) return false;
        if (!element.isWritable()) return false;

        if (element instanceof PsiJavaToken) {
            final PsiJavaToken token = (PsiJavaToken) element;
            int numberOfLines = StringUtil.countNewLines(token.getParent().getText());

            return token.getTokenType() == JavaTokenType.IF_KEYWORD && numberOfLines == 0;
        }
        return false;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }
}
