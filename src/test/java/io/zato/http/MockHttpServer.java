package io.zato.http;

import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import fi.iki.elonen.NanoHTTPD;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;

/**
 * A simple mock server, based on NanoHTTPD.
 *
 * @author jansorg
 */
public class MockHttpServer extends NanoHTTPD {
    private static final Logger LOG = Logger.getInstance("#zato.mockHttpd");

    static {
        //turn errors of in NanoHTTPDs log which are most often caused by broken HTTP connections (which are fine in our tests)
        java.util.logging.Logger nanoLog = java.util.logging.Logger.getLogger(NanoHTTPD.class.getName());
        nanoLog.setLevel(Level.OFF);
    }

    private final List<String> requestContent = Collections.synchronizedList(Lists.newArrayList());

    private final String username;
    private final String password;
    private String path;

    public MockHttpServer(String username, String password) {
        this(23000, "/ide-deploy", username, password);
    }

    public MockHttpServer(int port, String path, String username, String password) {
        super("127.0.0.1", port);
        this.path = path;
        this.username = username;
        this.password = password;
    }

    @Override
    public void start() throws IOException {
        super.start();
        requestContent.clear();
    }

    @Override
    protected boolean useGzipWhenAccepted(Response r) {
        return false;
    }

    /**
     * @return Returns the content which were send with the processed HTTP requests.
     */
    public List<String> getRequestContent() {
        return requestContent;
    }

    @Override
    public Response serve(IHTTPSession session) {
        boolean isPost = session.getMethod() == Method.POST;
        boolean isGet = session.getMethod() == Method.GET;
        if (!isPost && !isGet) {
            return newResponse(Response.Status.BAD_REQUEST, "Unsupported method");
        }

        if (!session.getUri().equals(path)) {
            return newResponse(Response.Status.NOT_FOUND, "Unsupported path");
        }

        String auth = session.getHeaders().get("authorization");
        if (auth == null) {
            return newResponse(Response.Status.UNAUTHORIZED, "Unauthorized");
        }

        if (auth.startsWith("Basic")) {
            String base64Credentials = auth.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), Charset.forName("UTF-8"));
            final String[] values = credentials.split(":", 2);
            if (!username.equals(values[0]) || !password.equals(values[1])) {
                return newResponse(Response.Status.UNAUTHORIZED, "Username or password invalid");
            }
        } else {
            return newResponse(Response.Status.BAD_REQUEST, "Unsupported authentication type");
        }

        try {
            Map<String, String> data = new HashMap<>();
            session.parseBody(data);

            String uploadedData = data.getOrDefault("postData", null);
            requestContent.add(uploadedData);

            //connection test
            if (uploadedData == null) {
                //language=JSON
                return newResponse(Response.Status.OK, "{\"zato_ide_deploy_create_response\": {\"msg\": \"Ping succeeded\",\"success\": true}}");
            }

            //language=JSON
            return newResponse(Response.Status.OK, "{\"zato_ide_deploy_create_response\": {\"msg\": \"Deploy succeeded\",\"success\": true}}");
        } catch (Exception e) {
            LOG.warn("Unable to send response", e);
            requestContent.add(null);

            return newResponse(Response.Status.INTERNAL_ERROR, "Exception while reading body");
        }
    }

    private Response newResponse(Response.IStatus status, String content) {
        Response response = newFixedLengthResponse(status, "application/json", content);
        response.setGzipEncoding(false);
        response.setKeepAlive(false);
        response.setChunkedTransfer(false);
        return response;
    }

    public URI getURI(@Nullable String path) {
        return URI.create(String.format("http://%s:%d%s", getHostname(), getListeningPort(), path != null ? path : "/"));
    }
}