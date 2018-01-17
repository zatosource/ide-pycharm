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
        Assert.assertEquals("http://www.example.com/", c.getUrl());
        Assert.assertEquals("http://www.example.com/zato/ide-deploy", c.getUploadUrl());

        //no trailing /
        c = new ZatoServerConfig("dev", "http://www.example.com", "user", "secret", true);
        Assert.assertEquals("http://www.example.com", c.getUrl());
        Assert.assertEquals("http://www.example.com/zato/ide-deploy", c.getUploadUrl());

        //path in url
        c = new ZatoServerConfig("dev", "http://www.example.com/zato/ide-deploy", "user", "secret", true);
        Assert.assertEquals("http://www.example.com/zato/ide-deploy", c.getUrl());
        Assert.assertEquals("http://www.example.com/zato/ide-deploy", c.getUploadUrl());
    }
}