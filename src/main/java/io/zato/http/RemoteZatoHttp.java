package io.zato.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.Base64;
import io.zato.intellij.settings.ZatoServerConfig;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Default implementation of {@link ZatoHttp} which uses a remote HTTP server and the Apache HTTP Components.
 *
 * @author jansorg
 */
public class RemoteZatoHttp implements ZatoHttp {
    private static final Logger LOG = Logger.getInstance("#zato.http.remote");

    //using Apache HTTP components shipped with IntelliJ
    private final HttpClientBuilder clientBuilder = HttpClients.custom()
            .setUserAgent("Zato IntelliJ plugin")
            .disableAutomaticRetries()
            .disableRedirectHandling()
            .setMaxConnTotal(2);

    @Override
    public ZatoHttpResponse testConnection(@NotNull ZatoServerConfig server) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Testing connection to Zato server. Server: %s", server));
        }

        try {
            String url = server.getUploadUrl();
            if (url == null) {
                throw new IOException("URL is not defined");
            }

            HttpGet request = new HttpGet(url);
            request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            return sendAuthenticatedRequest(server, request);
        } catch (IOException e) {
            LOG.debug(String.format("Error during connection test of %s", server.toString()), e);
            throw new IOException("IO exception during connection test", e);
        }
    }

    @Override
    public ZatoHttpResponse upload(@NotNull ZatoServerConfig server, @NotNull String content, @NotNull Path file) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Uploading file to Zato server. Server: %s, file: %s", server, file.toString()));
        }

        Path fileName = file.getFileName();
        if (fileName == null) {
            throw new IllegalStateException("Filename isn't available in path " + file);
        }

        JsonObject json = new JsonObject();
        json.addProperty("payload_name", fileName.toString());
        json.addProperty("payload", Base64.encode(content.getBytes(StandardCharsets.UTF_8)));

        HttpPost request = new HttpPost(server.getUploadUrl());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setEntity(new StringEntity(json.toString()));
        return sendAuthenticatedRequest(server, request);
    }

    /**
     * Sends a request which contains credentials using HTTP BASIC auth to the given Zato endpoint.
     *
     * @param server  The endpoint to send the request to. This is required to properly set up the authentication.
     * @param request The request to send
     * @return The HTTP response's status code.
     * @throws IOException Thrown if an IO exception occurred during HTTP setup or during the HTTP communication.
     */
    private ZatoHttpResponse sendAuthenticatedRequest(@NotNull ZatoServerConfig server, HttpRequestBase request) throws IOException {
        try (CloseableHttpClient client = clientBuilder.build()) {
            URI uri = new URI(server.getUploadUrl());
            HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());

            HttpClientContext context = HttpClientContext.create();
            if (server.hasCredentials()) {
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials(server.getUsername(), server.getSafePassword()));
                context.setCredentialsProvider(credentialsProvider);

                AuthCache authCache = new BasicAuthCache();
                authCache.put(host, new BasicScheme());
                context.setAuthCache(authCache);
            }

            HttpResponse response = client.execute(host, request, context);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                LOG.debug("Zato server responded with unsuccessful response code: " + statusCode);
                String message;
                switch (statusCode) {
                    case HttpStatus.SC_UNAUTHORIZED:
                        message = "Invalid username or password";
                        break;
                    case HttpStatus.SC_NOT_FOUND:
                        message = String.format("URL %s not found", server.getUploadUrl());
                        break;
                    default:
                        message = String.format("Server returned HTTP status code %d", statusCode);
                }
                return new ZatoHttpResponse(statusCode, message, false);
            }

            String body = EntityUtils.toString(response.getEntity());
            if (body == null || body.isEmpty()) {
                return new ZatoHttpResponse(statusCode, "", true);
            }

            JsonElement json = JsonParser.parseString(body);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Zato server response body: " + body);
            }

            JsonObject responseDetails = json.getAsJsonObject().getAsJsonObject("zato_ide_deploy_create_response");
            String message = responseDetails.getAsJsonPrimitive("msg").getAsString();
            boolean success = responseDetails.getAsJsonPrimitive("success").getAsBoolean();

            return new ZatoHttpResponse(statusCode, message, success);
        } catch (Exception e) {
            LOG.warn("Communication with server failed: " + server, e);
            throw new IOException("Failed to communicate with " + server.getUrl(), e);
        }
    }
}
