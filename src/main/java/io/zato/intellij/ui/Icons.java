package io.zato.intellij.ui;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public final class Icons {
    private Icons() {
    }

    public static final Icon ZatoLogo = IconLoader.findIcon("/icons/zato.svg", Icons.class.getClassLoader());
}
