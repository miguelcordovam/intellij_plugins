package com.restdocs.action.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.restdocs.action.common.RestServiceNode;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import static com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT;

public class FileUtil {

    public void createFile(Project project, String fileName, List<RestServiceNode> services) {
        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(fileName, PLAIN_TEXT, createHtmlFile(services));

        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        PsiFile file = directory.findFile(fileName);
        if (file == null) {
            ApplicationManager.getApplication().runWriteAction(() -> {
                directory.add(psiFile);
            });
        } else {
            ApplicationManager.getApplication().runWriteAction(() -> {
                file.delete();
                directory.add(psiFile);
            });
        }
    }

    private String createHtmlFile(List<RestServiceNode> services) {
        VelocityContext context = new VelocityContext();
        context.put("services", services);

        String templatePath = "template.html";
        InputStream input = getClass().getClassLoader().getResourceAsStream(templatePath);

        VelocityEngine engine = new VelocityEngine();
        Properties props = new Properties();
        props.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        props.put("runtime.log.logsystem.log4j.category", "velocity");
        props.put("runtime.log.logsystem.log4j.logger", "velocity");

        engine.init(props);
        StringWriter writer = new StringWriter();
        engine.evaluate(context, writer, "REST", new InputStreamReader(input));

        return writer.toString().replace("\n", "").replace("\r", "");
    }
}