package com.restdocs.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.impl.file.PsiJavaDirectoryFactory;
import com.intellij.psi.impl.file.impl.FileManagerImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.util.OpenSourceUtil;
import com.restdocs.action.common.RestService;
import com.restdocs.action.common.SpringAnnotations;
import com.restdocs.action.util.FileUtil;
import com.restdocs.action.util.PsiElementUtil;
import com.restdocs.action.util.Util;
import com.sun.jna.platform.mac.MacFileUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT;
import static com.restdocs.action.common.SpringAnnotations.REQUEST_MAPPING_QUALIFIED_NAME;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class GenerateRestDocsAction extends AnAction {


    private Util util = new Util();
    private FileUtil fileUtil = new FileUtil();
    private PsiElementUtil psiElementUtil = new PsiElementUtil();

    @Override
    public void actionPerformed(AnActionEvent e) {

        // TODO create export REST services as html file option on toolwindow
//        Project project = e.getProject();
//
//        fileUtil.createFile(project, "restDocumentation.html", util.getAllServices(project));
    }

    @Override
    public void update(AnActionEvent e) {

        Project project = e.getData(PROJECT);
        Editor editor = e.getData(EDITOR);

        e.getPresentation().setVisible(project != null);
    }


}
