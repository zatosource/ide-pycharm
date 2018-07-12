package io.zato.http;

import io.zato.intellij.settings.ZatoServerConfig;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author jansorg
 */
public class RemoteZatoHttpTest {
    private MockHttpServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockHttpServer("username", "secret");
        server.start();
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testConnection() throws IOException {
        ZatoHttp api = new RemoteZatoHttp();

        Assert.assertEquals("Expected a successful connection", HttpStatus.SC_OK,
                api.testConnection(newServerConfig("/", "username", "secret")).getStatusCode());
        Assert.assertEquals("Expected a successful connection", HttpStatus.SC_OK,
                api.testConnection(newServerConfig("/ide-deploy", "username", "secret")).getStatusCode());

        Assert.assertEquals("A request with an invalid path must fail", HttpStatus.SC_NOT_FOUND,
                api.testConnection(newServerConfig("/invalid/path", "myUser", "secret")).getStatusCode());

        Assert.assertEquals("A request with an invalid credentials must fail", HttpStatus.SC_UNAUTHORIZED,
                api.testConnection(newServerConfig("/ide-deploy", null, null)).getStatusCode());
    }

    @Test
    public void testUpload() throws IOException {
        ZatoHttp api = new RemoteZatoHttp();

        Assert.assertTrue("Expected a successful upload",
                api.upload(newServerConfig("/ide-deploy", "username", "secret"), "my content", Paths.get("/myFile.py")).getStatusCode() == HttpStatus.SC_OK);

        Assert.assertFalse("An upload request with an invalid path must fail",
                api.upload(newServerConfig("/invalid/path", "myUser", "secret"), "my content", Paths.get("/myFile.py")).getStatusCode() == HttpStatus.SC_OK);

        Assert.assertFalse("An upload request with an invalid credentials must fail",
                api.upload(newServerConfig("/ide-deploy", null, null), "my content", Paths.get("/myFile.py")).getStatusCode() == HttpStatus.SC_OK);
    }

    @NotNull
    private ZatoServerConfig newServerConfig(String path, String username, String password) {
        return new ZatoServerConfig("mock server", server.getURI(path).toString(), username, password, true);
    }
}