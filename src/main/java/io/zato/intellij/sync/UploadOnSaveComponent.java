package io.zato.intellij.sync;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.zato.file.CommentScanning;
import io.zato.intellij.http.ZatoHttpService;
import io.zato.intellij.settings.ZatoServerConfig;
import io.zato.intellij.settings.ZatoSettingsService;
import io.zato.intellij.vfs.VfsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author jansorg
 */
public class UploadOnSaveComponent extends ApplicationComponent.Adapter {
    private static final Logger LOG = Logger.getInstance("#zato.uploadOnSave");
    private static final String UPLOAD_MARKER = "# zato: ide-deploy=True";

    private final CommentScanning commentScanner = new CommentScanning(UPLOAD_MARKER);

    private final VirtualFileManager vfsManager;
    private final VirtualFileAdapter listener;

    public UploadOnSaveComponent(VirtualFileManager vfsManager, ZatoSettingsService settingsService, ZatoHttpService httpService, FileDocumentManager documentManager) {
        this.vfsManager = vfsManager;

        this.listener = new VirtualFileAdapter() {
            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                if (!event.isFromSave()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Ignoring Vfs change event for " + event.getFile().getPath());
                    }
                    return;
                }

                Optional<ZatoServerConfig> server = settingsService.getDefaultServer();
                if (!server.isPresent()) {
                    LOG.debug("Ignoring save event because the default server is missing.");
                    return;
                }

                if (!httpService.isSupported(event.getFile())) {
                    LOG.debug("Skipping unsupported file " + event.getFileName());
                    return;
                }

                Document document = documentManager.getDocument(event.getFile());
                if (document == null) {
                    LOG.debug("Couldn't locate document in save handler for " + event.getFile().getPath());
                    return;
                }

                if (commentScanner.matches(document.getText())) {
                    VirtualFile file = documentManager.getFile(document);
                    if (file == null) {
                        LOG.warn("Couldn't locate VirtualFile for document after commit: " + document);
                    } else {
                        //at this points it's a supported (Python) file which contains the upload marker
                        httpService.uploadAsync(server.get(), VfsUtils.getPath(file), document.getText(), ProjectUtil.guessProjectForContentFile(file));
                    }
                } else {
                    LOG.debug(String.format("Skipping upload-on-save because the marker '%s' wasn't found.", UPLOAD_MARKER));
                }
            }
        };
    }

    @Override
    public void disposeComponent() {
        LOG.debug("disposeComponent()");
        vfsManager.removeVirtualFileListener(listener);
    }

    @Override
    public void initComponent() {
        LOG.debug("initComponent()");
        vfsManager.addVirtualFileListener(listener);
    }
}
