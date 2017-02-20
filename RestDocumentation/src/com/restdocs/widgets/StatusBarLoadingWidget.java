package com.restdocs.widgets;

import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StatusBarLoadingWidget implements CustomStatusBarWidget {

    private JLabel myLabel = new JLabel("Loading...");

    @Override
    public JComponent getComponent() {
        myLabel.setSize(500, 10);

        return myLabel;
    }

    @NotNull
    @Override
    public String ID() {
        return "LoadingTW";
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
        return null;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {

    }

    @Override
    public void dispose() {

    }
}
