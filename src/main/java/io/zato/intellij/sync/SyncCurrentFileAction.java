package io.zato.intellij.sync;

import com.intellij.ide.scratch.ScratchUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.PythonFileType;
import io.zato.intellij.http.ZatoHttpService;
import io.zato.intellij.settings.ZatoServerConfig;
import io.zato.intellij.settings.ZatoSettingsService;
import io.zato.intellij.ui.Icons;

import java.util.Optional;

/**
 * An action to send the content of the current file to the default Zato.io server.
 * <p>
 * It's only enabled for Python files.
 *
 * @author jansorg
 */
public class SyncCurrentFileAction extends AnAction {
    private static PsiFile currentFile(DataContext e) {
        return e.getData(CommonDataKeys.PSI_FILE);
    }

    public SyncCurrentFileAction() {
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiFile file = currentFile(e.getDataContext());

        Optional<ZatoServerConfig> server = ZatoSettingsService.getInstance().getDefaultServer();
        if (!server.isPresent()) {
            Messages.showErrorDialog("There is no default server available in the settings.\nPlease configure at least one server.",
                                     "No Server Configuration");
        }
        else {
            ZatoHttpService.getInstance().uploadAsync(server.get(), file);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        PsiFile file = currentFile(e.getDataContext());

        Presentation presentation = e.getPresentation();
        presentation.setText("Upload to default Zato server");
        presentation.setIcon(Icons.ZatoLogo);
        presentation.setEnabled(file != null
                                && file.getFileType() instanceof PythonFileType
                                && !ScratchUtil.isScratch(file.getVirtualFile()));
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
