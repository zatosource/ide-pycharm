package io.zato.intellij.settings;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jansorg
 */
public class ZatoServerConfigTest {
    @Test
    public void uploadUrl() {
        ZatoServerConfig c = new ZatoServerConfig("dev", "http://www.example.com/", "user", "secret", true);
        Assert.assertTrue(c.isStoredPassword());
        Assert.assertEquals("http://www.example.com/", c.getUrl());
        Assert.assertEquals("http://www.example.com/ide-deploy", c.getUploadUrl());

        //no trailing /
        c = new ZatoServerConfig("dev", "http://www.example.com", "user", "secret", true);
        Assert.assertTrue(c.isStoredPassword());
        Assert.assertEquals("http://www.example.com", c.getUrl());
        Assert.assertEquals("http://www.example.com/ide-deploy", c.getUploadUrl());

        //path in url
        c = new ZatoServerConfig("dev", "http://www.example.com/ide-deploy", "user", "secret", true);
        Assert.assertTrue(c.isStoredPassword());
        Assert.assertEquals("http://www.example.com/ide-deploy", c.getUrl());
        Assert.assertEquals("http://www.example.com/ide-deploy", c.getUploadUrl());
    }

    @Test
    public void storedPassword() {
        ZatoServerConfig c = new ZatoServerConfig("dev", "http://www.example.com/ide-deploy", "user", "secret", true);
        Assert.assertTrue(c.isStoredPassword());

        c.setSafePassword(null);
        Assert.assertTrue(c.isStoredPassword());

        c.setSafePassword("");
        Assert.assertTrue("safe password must not change the flag", c.isStoredPassword());

        c.setOldPassword(null);
        Assert.assertFalse(c.isStoredPassword());

        c.setOldPassword("");
        Assert.assertFalse(c.isStoredPassword());

        c.setOldPassword("secret");
        Assert.assertTrue(c.isStoredPassword());
    }
}