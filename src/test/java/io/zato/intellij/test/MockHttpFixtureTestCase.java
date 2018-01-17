package io.zato.intellij.test;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import io.zato.http.MockHttpServer;
import io.zato.intellij.settings.ZatoServerConfig;
import io.zato.intellij.settings.ZatoSettingsService;

import java.util.ArrayList;

/**
 * Abstract base class for tests which use a mocked http server to emulate the Zato backend.
 *
 * @author jansorg
 */
public abstract class MockHttpFixtureTestCase extends LightPlatformCodeInsightFixtureTestCase {
    protected MockHttpServer httpd = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        httpd = new MockHttpServer("username", "secret");
        httpd.start();

        ZatoSettingsService.getInstance().getState().set(new ArrayList<>());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        httpd.stop();
    }

    protected void configureDefaultServer() {
        ZatoSettingsService settings = ZatoSettingsService.getInstance();

        settings.getState().add(new ZatoServerConfig("main server", httpd.getURI("/").toString(), "username", "secret", true));

    }
}
