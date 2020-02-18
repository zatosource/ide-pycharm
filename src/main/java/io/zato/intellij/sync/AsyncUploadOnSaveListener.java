package io.zato.intellij.sync;

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

                    Document document = documentManager.getDocument(file);
                    if (document == null) {
                        LOG.debug("Couldn't locate document in save handler for " + file.getPath());
                        return;
                    }

                    if (commentScanner.matches(document.getText())) {
                        file = documentManager.getFile(document);
                        if (file == null) {
                            LOG.warn("Couldn't locate VirtualFile for document after commit: " + document);
                        } else {
                            //at this points it's a supported (Python) file which contains the upload marker
                            httpService.uploadAsync(server.get(),
                                    VfsUtils.getPath(file),
                                    document.getText(),
                                    ProjectUtil.guessProjectForContentFile(file));
                        }
                    } else {
                        LOG.debug(String.format("Skipping upload-on-save because the marker '%s' wasn't found.", UPLOAD_MARKER));
                    }
                });
            }
        };
    }
}
