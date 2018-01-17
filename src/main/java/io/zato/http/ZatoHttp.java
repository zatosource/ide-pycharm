package io.zato.http;

import io.zato.intellij.settings.ZatoServerConfig;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface to a Zato.io server. It allows to test the connection and to upload content to a given server.
 *
 * @author jansorg
 */
public interface ZatoHttp {
    /**
     * Tests the given connection by uploading an empty file.
     *
     * @param server Serer configuration to use
     * @return The type of the response which was returned by the server.
     */
    ZatoHttpResponse testConnection(@NotNull ZatoServerConfig server) throws IOException;

    /**
     * Uploads a file to the given server.
     *
     * @param server  Server to upload to
     * @param content The content to upload
     * @param file    The file being uploaded. An empty file means that the connection is being tested.
     * @return {@code true} if the HTTP server returned status 200, {@code false} otherwise.
     * @throws IOException May occur while communicating with the HTTP server.
     */
    ZatoHttpResponse upload(@NotNull ZatoServerConfig server, @NotNull String content, @NotNull Path file) throws IOException;
}
