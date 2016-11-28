package com.mcordova.replacetext;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;

import java.awt.datatransfer.StringSelection;

public class ReplaceTextAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final SelectionModel selectionModel = editor.getSelectionModel();

        String selected = selectionModel.getSelectedText();
        final String cleanText = selected.replaceAll("\\+|\"", "");

        CopyPasteManager.getInstance().setContents(new StringSelection(cleanText));
    }

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);

        e.getPresentation().setVisible((project != null && editor !=
                null && editor.getSelectionModel().hasSelection()));
    }
}
