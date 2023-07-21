package io.zato.http;

import com.intellij.util.Base64;
import io.zato.intellij.settings.ZatoServerConfig;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    /**
     * We were using JetBrains' Base64 and want to make sure that Java's Base64 is returning the same string
     */
    @SuppressWarnings("deprecation")
    @Test
    public void base64() {
        String randomString = "WEJIJI3TmI0U\n" +
                "PLv66gC7BNoS\n" +
                "0cVUVv2vGWfd\n" +
                "9BUlaj9g0fqI\n" +
                "rNeqCUbdrorW\n" +
                "iBMJOfgVuK2j\n" +
                "ZvjznKQloLLI\n" +
                "gCnFmQwm3EwR\n" +
                "9Inu6y3TOQJS\n" +
                "Yl0xWRDXPaS1\n" +
                "4WT8UJyWihFO\n" +
                "UpD2p8g6eWvX\n" +
                "v4yz8Uqu5NFS\n" +
                "EYUdhvsnwu8G\n" +
                "VFdweg7iX491\n" +
                "M5jpkaDWew0I";

        String jetbrainsBase64 = Base64.encode(randomString.getBytes(StandardCharsets.UTF_8));
        String javaBase64 = java.util.Base64.getEncoder().encodeToString(randomString.getBytes(StandardCharsets.UTF_8));

        Assert.assertEquals(jetbrainsBase64, javaBase64);
    }

    @NotNull
    private ZatoServerConfig newServerConfig(String path, String username, String password) {
        return new ZatoServerConfig("mock server", server.getURI(path).toString(), username, password, true);
    }
}