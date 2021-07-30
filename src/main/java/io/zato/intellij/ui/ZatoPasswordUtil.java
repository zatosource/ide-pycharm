package io.zato.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public final class ZatoPasswordUtil {
    public static String promptPassword(@Nullable Project project) {
        String title = "Zato Server";
        String message = "Please enter the password of the default Zato server:";

        if (ApplicationManager.getApplication().isDispatchThread()) {
            return Messages.showPasswordDialog(project, message, title, null);
        }

        AtomicReference<String> ref = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            String input = Messages.showPasswordDialog(project, message, title, null);
            ref.set(input);
        });
        return ref.get();
    }
}
