package io.zato.intellij.sync;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import io.zato.file.CommentScanning;
import io.zato.intellij.http.ZatoHttpService;
import io.zato.intellij.settings.ZatoServerConfig;
import io.zato.intellij.settings.ZatoSettingsService;
import io.zato.intellij.ui.ZatoPasswordUtil;
import io.zato.intellij.vfs.VfsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AsyncUploadOnSaveListener implements AsyncFileListener {
    private static final Logger LOG = Logger.getInstance("#zato.uploadOnSave");
    private static final String UPLOAD_MARKER = "# zato: ide-deploy=True";

    private final CommentScanning commentScanner = new CommentScanning(UPLOAD_MARKER);

    @Nullable
    @Override
    public ChangeApplier prepareChange(@NotNull List<? extends VFileEvent> events) {
        List<? extends VFileEvent> matching = events
            .stream()
            .filter(e -> e.isFromSave() && e.isValid())
            .collect(Collectors.toList());

        if (matching.isEmpty()) {
            return null;
        }

        return new ChangeApplier() {
            @Override
            public void afterVfsChange() {
                // move into background because afterVfsChange is executed in a write action
                // the password prompt is not possible in a write action
                if (ApplicationManager.getApplication().isUnitTestMode()) {
                    handleUpload(matching);
                }
                else {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> handleUpload(matching));
                }
            }
        };
    }

    private void handleUpload(List<? extends VFileEvent> matching) {
        ZatoSettingsService settingsService = ZatoSettingsService.getInstance();
        ZatoHttpService httpService = ZatoHttpService.getInstance();
        FileDocumentManager documentManager = FileDocumentManager.getInstance();

        matching.forEach(event -> {
            Optional<ZatoServerConfig> server = settingsService.getDefaultServer();
            if (!server.isPresent()) {
                LOG.debug("Ignoring save event because the default server is missing.");
                return;
            }

            if (!httpService.isSupported(event.getFile())) {
                LOG.debug("Skipping unsupported file " + event.getPath());
                return;
            }

            VirtualFile file = event.getFile();
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
                }
                else {
                    ZatoServerConfig serverConfig = server.get();
                    boolean promptPassword = !ApplicationManager.getApplication().isUnitTestMode()
                                             && serverConfig.isStoredPassword()
                                             && !serverConfig.hasCredentials();
                    if (promptPassword) {
                        String password = ZatoPasswordUtil.promptPassword(null);
                        serverConfig.setSafePassword(password);
                    }

                    // at this point it's a supported (Python) file which contains the upload marker
                    httpService.uploadAsync(serverConfig,
                                            VfsUtils.getPath(file),
                                            document.getText(),
                                            ProjectUtil.guessProjectForContentFile(file));
                }
            }
            else {
                LOG.debug(String.format("Skipping upload-on-save because the marker '%s' wasn't found.", UPLOAD_MARKER));
            }
        });
    }
}
