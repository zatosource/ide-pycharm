package io.zato.intellij.sync;

import com.intellij.ide.actions.SaveAllAction;
import com.intellij.ide.actions.SaveDocumentAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.zato.file.CommentScanning;
import io.zato.intellij.http.ZatoHttpService;
import io.zato.intellij.settings.ZatoServerConfig;
import io.zato.intellij.settings.ZatoSettingsService;
import io.zato.intellij.ui.ZatoPasswordUtil;
import io.zato.intellij.vfs.VfsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class UploadOnSaveActionListener implements AnActionListener, DumbAware {
    private static final Logger LOG = Logger.getInstance("#zato.uploadOnSave");
    private static final String UPLOAD_MARKER = "# zato: ide-deploy=True";
    private static final CommentScanning commentScanner = new CommentScanning(UPLOAD_MARKER);

    @Override
    public void afterActionPerformed(@NotNull AnAction action,
                                     @NotNull AnActionEvent event,
                                     @NotNull AnActionResult result) {
        if (!(action instanceof SaveAllAction) && !(action instanceof SaveDocumentAction)) {
            return;
        }

        VirtualFile file = findSavedFile(event.getDataContext());
        if (file != null) {
            if (ApplicationManager.getApplication().isUnitTestMode()) {
                syncFileInBackground(file);
            } else {
                ApplicationManager.getApplication().executeOnPooledThread(() -> syncFileInBackground(file));
            }
        }
    }

    @Nullable
    private static VirtualFile findSavedFile(@NotNull DataContext dataContext) {
        VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (file != null) {
            return file;
        }

        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor != null) {
            Document document = editor.getDocument();
            return FileDocumentManager.getInstance().getFile(document);
        }

        return null;
    }

    private static void syncFileInBackground(@Nullable VirtualFile file) {
        ZatoSettingsService settingsService = ZatoSettingsService.getInstance();
        ZatoHttpService httpService = ZatoHttpService.getInstance();
        Optional<ZatoServerConfig> server = settingsService.getDefaultServer();
        FileDocumentManager documentManager = FileDocumentManager.getInstance();

        if (server.isEmpty()) {
            LOG.debug("Ignoring save event because the default server is missing.");
            return;
        }

        if (!httpService.isSupported(file)) {
            LOG.debug("Skipping unsupported file " + (file != null ? file.getPath() : ""));
            return;
        }

        if (file == null) {
            return;
        }

        final VirtualFile finalFile = file;
        Document document = ReadAction.compute(() -> documentManager.getDocument(finalFile));
        if (document == null) {
            LOG.debug("Couldn't locate document in save handler for " + file.getPath());
            return;
        }

        if (commentScanner.matches(document.getText())) {
            file = documentManager.getFile(document);
            if (file == null) {
                LOG.warn("Couldn't locate VirtualFile for document after commit: " + document);
            } else {
                ZatoServerConfig serverConfig = server.get();
                boolean promptPassword = !ApplicationManager.getApplication().isUnitTestMode()
                        && serverConfig.isStoredPassword()
                        && !serverConfig.hasCredentials();
                if (promptPassword) {
                    String password = ZatoPasswordUtil.promptPassword(null);
                    serverConfig.setSafePassword(password);
                    serverConfig.storeSafePassword();
                }

                // at this point it's a supported (Python) file which contains the upload marker
                httpService.uploadAsync(serverConfig,
                        VfsUtils.getPath(file),
                        document.getText(),
                        ProjectUtil.guessProjectForContentFile(file));
            }
        } else {
            LOG.debug(String.format("Skipping upload-on-save because the marker '%s' wasn't found.", UPLOAD_MARKER));
        }
    }
}
