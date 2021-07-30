package io.zato.intellij.http;

import com.intellij.ide.scratch.ScratchUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.PythonFileType;
import io.zato.http.RemoteZatoHttp;
import io.zato.http.ZatoHttp;
import io.zato.http.ZatoHttpResponse;
import io.zato.intellij.settings.ZatoServerConfig;
import io.zato.intellij.ui.Icons;
import io.zato.intellij.vfs.VfsUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author jansorg
 */
public class ZatoHttpService {
    private static final Logger LOG = Logger.getInstance("#zato.httpService");

    private final ZatoHttp requestHandler;

    public ZatoHttpService() {
        this.requestHandler = new RemoteZatoHttp();
    }

    public static ZatoHttpService getInstance() {
        return ApplicationManager.getApplication().getService(ZatoHttpService.class);
    }

    public boolean isSupported(PsiFile file) {
        if (file == null) {
            return false;
        }

        if (!isSupported(file.getVirtualFile())) {
            LOG.info("Skipping upload of unsupported virtual file");
            return false;
        }

        if (!file.isPhysical()) {
            LOG.info("Skipping upload of non-physical file");
            return false;
        }

        return true;
    }

    public boolean isSupported(VirtualFile file) {
        if (file == null) {
            LOG.info("Skipping empty Vfs file");
            return false;
        }

        if (file.getFileType() != PythonFileType.INSTANCE) {
            LOG.info("Skipping upload of non-python file");
            return false;
        }

        if (file.isDirectory()) {
            LOG.info("Skipping upload of directory: " + file.getPath());
            return false;
        }

        if (!file.isInLocalFileSystem()) {
            LOG.info("Skipping upload of non-local file");
            return false;
        }

        if (ScratchUtil.isScratch(file)) {
            LOG.info("Skipping upload of scratch-file");
            return false;
        }

        return true;
    }

    /**
     * Tests the conneection to the given server.
     *
     * @param server The server configuration to test
     * @return The server's response
     */
    public ZatoHttpResponse testConnection(ZatoServerConfig server) throws IOException {
        return requestHandler.testConnection(server);
    }

    public void uploadAsync(ZatoServerConfig server, PsiFile file) {
        if (!isSupported(file)) {
            return;
        }

        uploadAsync(server, VfsUtils.getPath(file), file.getText(), file.getProject());
    }

    /**
     * Does an asynchronous upload of the file content, belonging to the file given as {@code path} to the server
     * passed as {@code server}.
     *
     * @param server      The server to upload to
     * @param path        The path of the file being uploaded
     * @param fileContent The content to upload to the remote server
     * @param project
     */
    public void uploadAsync(ZatoServerConfig server, Path path, String fileContent, Project project) {
        UploadRunnable runnable = new UploadRunnable(path, fileContent, server, project);

        Application application = ApplicationManager.getApplication();
        if (application.isUnitTestMode()) {
            //run in the current thread in test cases because async code is hardly testable
            runnable.run();
        } else {
            //run in a background thread
            application.executeOnPooledThread(runnable);
        }
    }

    private static class ZatoDeployErrorNotification extends Notification {
        public ZatoDeployErrorNotification(ZatoServerConfig server, @NotNull String errorMessage) {
            super("Zato", Icons.ZatoLogo,
                    "Zato hot-deploy failed",
                    null,
                    String.format("<html>Hot-deployment to %s failed.\n<br>Error: <em>%s</em></html>", server.getName(), StringUtils.trimToEmpty(errorMessage)),
                    NotificationType.ERROR,
                    null);
        }
    }

    private class UploadRunnable implements Runnable {
        private final Path path;
        private final String content;
        private final ZatoServerConfig server;
        private final Project project;

        UploadRunnable(Path path, String content, ZatoServerConfig server, Project project) {
            this.path = path;
            this.content = content;
            this.server = server;
            this.project = project;
        }

        @Override
        public void run() {
            ZatoHttpResponse response = null;
            String message;
            try {
                response = requestHandler.upload(server, content, path);
                message = response.getMessage();
            } catch (IOException e) {
                LOG.warn(String.format("HTTP upload failed. Path: %s", path), e);
                message = e.getMessage();
            }

            expireVisibleNotifications();
            if (response == null || !response.isSuccessfullyDeployed()) {
                new ZatoDeployErrorNotification(server, message).notify(project);

                StatusBar.Info.set(String.format("Hot-deployment to %s failed: %s", server.getName(), message), project);
            } else {
                StatusBar.Info.set(String.format("Hot-deployed successfully to %s.", server.getName()), project);
            }
        }

        private void expireVisibleNotifications() {
            ZatoDeployErrorNotification[] notifications = NotificationsManager.getNotificationsManager().getNotificationsOfType(ZatoDeployErrorNotification.class, project);
            for (ZatoDeployErrorNotification notification : notifications) {
                notification.expire();
            }
        }
    }
}
